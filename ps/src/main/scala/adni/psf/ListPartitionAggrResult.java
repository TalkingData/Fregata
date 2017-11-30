package adni.psf;

import adni.utils.SimpleEntry;
import com.tencent.angel.ml.matrix.psf.get.base.PartitionGetResult;
import io.netty.buffer.ByteBuf;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;


/**
 * Created by chris on 11/7/17. sorted on each partition
 */
public class ListPartitionAggrResult extends PartitionGetResult {
    public List<Entry<Integer,Entry<Float,Float>>> result;
    public ListPartitionAggrResult(List<Entry<Integer,Entry<Float,Float>>> result) {this.result = result;}
    public ListPartitionAggrResult(){this.result = null;}

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeInt(result.size());
        for (Entry<Integer,Entry<Float,Float>> entry: result) {
            buf.writeInt(entry.getKey());
            buf.writeFloat(entry.getValue().getKey());
            buf.writeFloat(entry.getValue().getValue());
        }
    }

    @Override
    public void deserialize(ByteBuf buf) {
        int resultSize = buf.readInt();
        result = new LinkedList<>();
        for (int i = 0; i < resultSize; i++) {
            int id = buf.readInt();
            float degree = buf.readFloat();
            float s = buf.readFloat();
            Entry<Float,Float> vInfo = new SimpleEntry<>(degree,s);

            Entry<Integer,Entry<Float,Float>> entry = new SimpleEntry<>(id,vInfo);
            result.add(entry);
        }
    }

    @Override
    public int bufferLen() {
        return 4 + result.size() * 12;
    }

}
