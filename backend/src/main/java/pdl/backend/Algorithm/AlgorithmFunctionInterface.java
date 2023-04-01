package pdl.backend.Algorithm;

import java.util.List;

import boofcv.struct.image.GrayU8;
import boofcv.struct.image.Planar;

public interface AlgorithmFunctionInterface {
	void apply(Planar<GrayU8> image, List<Object> parameters);
}
