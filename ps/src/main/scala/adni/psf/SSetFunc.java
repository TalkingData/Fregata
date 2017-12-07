package adni.psf;

import adni.utils.SimpleEntry;
import adni.utils.util;
import com.tencent.angel.PartitionKey;
import com.tencent.angel.exception.AngelException;
import com.tencent.angel.ml.matrix.psf.get.base.*;
import com.tencent.angel.ps.impl.PSContext;
import com.tencent.angel.ps.impl.matrix.ServerDenseFloatRow;
import com.tencent.angel.ps.impl.matrix.ServerRow;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by chris on 11/7/17.
 */
public class SSetFunc extends GetFunc{
    private static final Log LOG = LogFactory.getLog(SSetFunc.class);
    public SSetFunc(int matrixId) {
        super(new GetParam(matrixId));
    }
    public SSetFunc(){super(null);}

    @Override
    public PartitionGetResult partitionGet(PartitionGetParam partParam) {
        PartitionKey pkey = partParam.getPartKey();

        pkey = PSContext.get().getMatrixPartitionManager().
                getPartition(pkey.getMatrixId(),
                        pkey.getPartitionId()).getPartitionKey();
            ServerRow mVec = PSContext.get().getMatrixPartitionManager().getRow(pkey, 0);
            ServerRow dVec = PSContext.get().getMatrixPartitionManager().getRow(pkey, 1);
            List<Map.Entry<Integer,Map.Entry<Float,Float>>> partResult = sVec(mVec,dVec);
            return new ListPartitionAggrResult(partResult);
    }

    /**
     * Sort each partition on server
     * @param mVec
     * @param dVec
     * @return
     */

    private List<Map.Entry<Integer,Map.Entry<Float,Float>>> sVec(ServerRow mVec, ServerRow dVec) {
        int start = (int) mVec.getStartCol();
        int end = (int) mVec.getEndCol();
        int len = end - start;
        List<Map.Entry<Integer,Map.Entry<Float,Float>>> partResult = new ArrayList<>();
        if(mVec instanceof ServerDenseFloatRow && dVec instanceof ServerDenseFloatRow) {
            FloatBuffer mBuf  = ((ServerDenseFloatRow) mVec).getData();
            FloatBuffer dBuf  = ((ServerDenseFloatRow) dVec).getData();
            for(int i = 0; i < len; i ++) {
                if(mBuf.get(i) > 0.0f){
                    Map.Entry<Float,Float> vInfo = new SimpleEntry<>(dBuf.get(i),mBuf.get(i) / dBuf.get(i));
                    partResult.add(new SimpleEntry<>(start + i, vInfo));
                }
            }
        } else
            throw new AngelException("should be ServerDenseFloatRow");
        util.entriesSortedByValues(partResult);
        return partResult;
    }

    @Override
    public GetResult merge(List<PartitionGetResult> partResults) {
        List<List<Map.Entry<Integer,Map.Entry<Float,Float>>>> toMergeLists = new ArrayList<>();

        for(PartitionGetResult r : partResults) {
                toMergeLists.add(((ListPartitionAggrResult) r).result);
        }
        List<Map.Entry<Integer,Map.Entry<Float,Float>>> mergedList = util.mergeMultipleLists(toMergeLists);
        return new ListAggrResult(mergedList);
    }

}
