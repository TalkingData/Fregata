package adni.psf;

import com.tencent.angel.ml.matrix.psf.get.base.GetResult;

import java.util.List;
import java.util.Map;

/**
 * Created by chris on 11/8/17.
 * get list result which has been sorted
 */
public class ListAggrResult extends GetResult {
    private final List<Map.Entry<Integer,Map.Entry<Float,Float>>> result;
    public ListAggrResult(List<Map.Entry<Integer,Map.Entry<Float,Float>>> result) {
        this.result = result;
    }

    public List<Map.Entry<Integer,Map.Entry<Float,Float>>> getResult() {return result;}
}
