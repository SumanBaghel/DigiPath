package com.example.digipath;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.digipath.ml.Model;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Activity2 extends AppCompatActivity {

    private ImageView imageView, imageView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2);

        imageView = findViewById(R.id.imageView);
        imageView2 = findViewById(R.id.imageView2);

        String imgUri = getIntent().getStringExtra("imgUri");
        Uri imageUri = Uri.parse(imgUri);
        Bitmap bitmap = uriToBitmap(this, imageUri);
//        imageView.setImageBitmap(bitmap);

        // Load and initialize the TensorFlow Lite model
        try {
            Model model = Model.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 384, 384, 3}, DataType.FLOAT32);
            ByteBuffer inputBuffer = preprocessInput(bitmap);
            inputFeature0.loadBuffer(inputBuffer);

            // Runs model inference and gets result.
            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            // Postprocessing
            Bitmap maskBitmap = postprocessingOutput(outputFeature0);

            // Display the segmented mask image in imageView2
            imageView2.setImageBitmap(maskBitmap);

            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
    }



    //----------------------------------------------------------------------------------


    private ByteBuffer preprocessInput(Bitmap bitmap) {
        // Resize the input image
        int dimension = Math.min(bitmap.getWidth(), bitmap.getHeight());
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, dimension, dimension);
        bitmap = Bitmap.createScaledBitmap(bitmap, 384,384, false);
        imageView.setImageBitmap(bitmap);

        // Convert the bitmap to a ByteBuffer
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * 384 * 384 * 3);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[384 * 384];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        // iterate over each pixel and extract R,G,B values. add those values individually to byte buffer.
        int pixel = 0;
        for (int i=0; i<384; i++) {
            for (int j = 0; j<384; j++){
                int val = intValues[pixel++]; //RGB
                byteBuffer.putFloat(((val >> 16) & 0xFF)*(1.f/255));
                byteBuffer.putFloat(((val >> 8) & 0xFF)*(1.f/255));
                byteBuffer.putFloat((val & 0xFF)*(1.f/255));
            }
        }
        byteBuffer.rewind();
        return byteBuffer;
    }

    //----------------------------------------------------------------------------------


    private Bitmap postprocessingOutput(TensorBuffer outputBuffer) {
        float[] probabilities = outputBuffer.getFloatArray();

        // Reshape probabilities array to 3D shape (384, 384, 3)
        int outputHeight = 384;
        int outputWidth = 384;
        int numChannels = 3;

        float[][][] reshapedProbabilities = new float[outputHeight][outputWidth][numChannels];

        int bufferIndex = 0;
        for (int h = 0; h < outputHeight; h++) {
            for (int w = 0; w < outputWidth; w++) {
                for (int c = 0; c < numChannels; c++) {
                    reshapedProbabilities[h][w][c] = probabilities[bufferIndex++];
                }
            }
        }

        // Finding the index of the maximum element along axis -1
        int[][] argmax = new int[outputHeight][outputWidth];
        for (int h = 0; h < outputHeight; h++) {
            for (int w = 0; w < outputWidth; w++) {
                int maxIndex = 0;
                float maxValue = reshapedProbabilities[h][w][0];
                for (int c = 1; c < numChannels; c++) {
                    if (reshapedProbabilities[h][w][c] > maxValue) {
                        maxValue = reshapedProbabilities[h][w][c];
                        maxIndex = c;
                    }
                }
                argmax[h][w] = maxIndex;
            }
        }

        // Reshape argmax array to the desired shape
        int[][] reshapedArgmax = new int[argmax.length][argmax[0].length];
        for (int i = 0; i < reshapedArgmax.length; i++) {
            System.arraycopy(argmax[i], 0, reshapedArgmax[i], 0, reshapedArgmax[i].length);
        }

        int[][] segmentationMap = new int[reshapedArgmax.length][reshapedArgmax[0].length];

        // Initialize the segmentation map with zeros
        for (int i = 0; i < segmentationMap.length; i++) {
            Arrays.fill(segmentationMap[i], 0);
        }

        // Find unique class labels in reshapedArgmax
        HashSet<Integer> uniqueLabels = new HashSet<>();
        for (int i = 0; i < reshapedArgmax.length; i++) {
            for (int j = 0; j < reshapedArgmax[i].length; j++) {
                uniqueLabels.add(reshapedArgmax[i][j]);
            }
        }

        // Assign class labels to the corresponding positions in the segmentation map
        for (int label : uniqueLabels) {
            for (int i = 0; i < reshapedArgmax.length; i++) {
                for (int j = 0; j < reshapedArgmax[i].length; j++) {
                    if (reshapedArgmax[i][j] == label) {
                        segmentationMap[i][j] = label;
                    }
                }
            }
        }

        // Create a bitmap from the segmentation map
        int[] pixels = new int[outputHeight * outputWidth];

        for (int h = 0; h < outputHeight; h++) {
            for (int w = 0; w < outputWidth; w++) {
                int label = segmentationMap[h][w];
                int color = getColorForLabel(label); // Get color based on label
                pixels[h * outputWidth + w] = color;
            }
        }

        return Bitmap.createBitmap(pixels, 384, 384, Bitmap.Config.ARGB_8888);
    }

    //----------------------------------------------------------------------------------

    private int getColorForLabel(int label) {
        // Define a mapping of label to color
        int[] colors = {Color.RED, Color.GREEN, Color.BLUE};

        if (label >= 0 && label < colors.length) {
            return colors[label];
        }

        return Color.BLACK; // Return black color for unknown labels
    }

    private Bitmap uriToBitmap(Context context, Uri uri) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options);
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
