package schneiderlab.tools.radicalprojection.imageprocessor.core.createsideview;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageConverter;
import ij.process.StackProcessor;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;

public class DetectAndRotate {
    private String filePath;

    public DetectAndRotate (String filePath){
        this.filePath = filePath;
    }
    public Img<FloatType> process() {
        ImagePlus inputImageStack = new ImagePlus(this.filePath);
        if(inputImageStack.getWidth() > inputImageStack.getHeight()){
        StackProcessor sp = new StackProcessor(inputImageStack.getStack(), null);
        ImageStack rotatedStack = sp.rotateLeft();
        inputImageStack.setStack(rotatedStack);
        ImageConverter converter = new ImageConverter(inputImageStack);
        converter.convertToGray32();
        return ImagePlusAdapter.wrapImgPlus(inputImageStack);
    } else {
            return ImagePlusAdapter.wrapImgPlus(inputImageStack);
        }
    }
}
