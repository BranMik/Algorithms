package week2;

import java.util.Arrays;
import edu.princeton.cs.algs4.Picture;

/**
* Implementation of the Seam Carver algorithm which performs content-aware image resizing.
* 
* @author Branko Mikusic
*/
public class SeamCarver {
	   
   private final float BORDER_PIXEL_MAX_ENERGY = 1000f;
   private Picture pictureData;
   private boolean pictureObjIsValid = true;
   private boolean isEnergyCalculated = false;
   
   //getRgb() values for current state of pictureData. 
   private int[][] pixelsMatrix;
   
   //Horizontal energy values for current state of pictureData. Empty until first horizontal find seam execution.
   private float[][] horEnergyMatrix = null;
   
   //Vertical energy values for current state of pictureData. Empty until first vertical find seam execution.
   private float[][] verEnergyMatrix = null;
   
   private int currentPictureWidth;
   private int currentPictureHeight;
   
   private int[] lastHorizontalSeamRemoved;
   private int[] lastVerticalSeamRemoved;
   
   /**
	 * Constructor creates picture object (pictureData) from picture argument and initializes some variables. 
	 * 
	 * @param picture Picture object passed in for seam removal.
	 */
   public SeamCarver(Picture picture) {
	   if(picture == null) throw new IllegalArgumentException(); 
	   this.pictureData = new Picture(picture);
	   pictureData.save("chukCheck.jpg");
	   currentPictureWidth = this.pictureData.width();
	   currentPictureHeight =  this.pictureData.height();
	   lastHorizontalSeamRemoved = new int[currentPictureWidth];
	   lastVerticalSeamRemoved = new int[currentPictureHeight];
	   initPixelsMatrix();
   }

   private void initPixelsMatrix() {
	   pixelsMatrix=new int[currentPictureWidth][currentPictureHeight];
	   for(int x = 0 ; x < currentPictureWidth ; x++) {
		   for(int y = 0 ; y < currentPictureHeight ; y++) {
			   pixelsMatrix[x][y] = pictureData.getRGB(x , y);
		   }
	   }
   }
   
   private void resetGlobalVariables() {
	   initPixelsMatrix();
	   horEnergyMatrix = null;
	   verEnergyMatrix = null;
	   lastHorizontalSeamRemoved = new int[currentPictureWidth];
	   lastVerticalSeamRemoved = new int[currentPictureHeight];
	   pictureObjIsValid = true;
	   isEnergyCalculated = false;
   }
   
   /**
	 * First creates new picture object from matrices that hold current picture state, and then it
	 * returns the updated picture.
	 * 
	 * @return new picture with seams removed.
	 */
   public Picture picture() {
	   
	   // A seam was removed from the picture so the pictureData object of type Picture has to be transposed.
	   if(!pictureObjIsValid) { 
		   Picture newPic = new Picture(currentPictureWidth,currentPictureHeight);
		   for(int x = 0 ; x < currentPictureWidth ; x++) {
			   for(int y = 0 ; y < currentPictureHeight ; y++) {
				   newPic.setRGB(x , y , pixelsMatrix[x][y]);
			   }
		   }
		   pictureData = newPic;
		   pictureObjIsValid=true;
	   }
	   
	   resetGlobalVariables();
	   return new Picture(pictureData);
   }

   /**
    * Returns the width of the picture. As the picture is not recreated until it's returned this method returns
    * intermediary updated data held in a variable, not pictureObject.width().
    *
    * @return the width of the picture (in pixels)
    */
   public int width() {
	   return currentPictureWidth;
   }

   /**
    * Returns the height of the picture. 
    *
    * @return the height of the picture (in pixels)
    */
   public int height() {
	   return currentPictureHeight;
   }

   /**
    * Returns energy of pixel at pixelColumn x and pixelRow y.
    *
    * @param x column of pixel
    * @param y row of pixel
    * @return energy of pixel.
    */
   public double energy(int x, int y) {
	   double energy;
	   if(x < 0 || y < 0 || x >= currentPictureWidth || y >= currentPictureHeight) throw new IllegalArgumentException(); 
	   else if(x == 0 || x == currentPictureWidth-1 || y == 0 || y == currentPictureHeight - 1) return BORDER_PIXEL_MAX_ENERGY;
	   else {
		   energy = Math.sqrt(calculateEnergyBetween2Pixels(x + 1, y, x - 1, y) + calculateEnergyBetween2Pixels(x, y + 1, x, y - 1));
	   }
	   return energy;
   }
      
   private double calculateEnergyBetween2Pixels(int x1, int y1, int x2, int y2) {
	   int pixel1,pixel2;
	   pixel1 = pixelsMatrix[x1][y1];
	   pixel2 = pixelsMatrix[x2][y2];
	   
	   int r1 = (pixel1 >> 16) & 0xFF ,  g1 = (pixel1 >>  8) & 0xFF , b1 = (pixel1 >>  0) & 0xFF;
	   int r2 = (pixel2 >> 16) & 0xFF , g2 = (pixel2 >>  8) & 0xFF , b2 = (pixel2 >>  0) & 0xFF;
	   double energy = Math.pow(r1 - r2 , 2) + Math.pow(g1 - g2 , 2) + Math.pow(b1 - b2 , 2);
	   return energy;
   }
   
   /*
    * Generalized findSeam method. Works for both horizontal and vertical seam removals, gets called with different 
    * references as arguments for different cases.
    */
   private int[] findSeam(int[] lastSeamRemoved, float[][] energyMatrix, int matrixWidth, int matrixHeight, boolean reversePixels) {
	   int bestpixelColIndx = 0;
	   float bestpixelColVal = 0;
	   
	   // Fills the least energy matrix.
	   if(!isEnergyCalculated) {
		   Arrays.fill(energyMatrix[0], BORDER_PIXEL_MAX_ENERGY);
	   }
	   for(int matrixRow = 1; matrixRow < matrixHeight; matrixRow++) {
		   int matrixCol; 
		   if(lastSeamRemoved[matrixRow - 1] - 1 < 1) {
			   energyMatrix[matrixRow][0] = BORDER_PIXEL_MAX_ENERGY + energyMatrix[matrixRow - 1][1];
			   matrixCol = 1;
		   }else {
			   matrixCol = (lastSeamRemoved[matrixRow - 1] - 1);
		   }
		   energyMatrix[matrixRow][matrixWidth - 1] = BORDER_PIXEL_MAX_ENERGY + energyMatrix[matrixRow - 1][matrixCol - 1];
		   
		   for(; matrixCol < matrixWidth -1; matrixCol++) {
			   float pixelEnergy = (!reversePixels)? (float) this.energy(matrixCol, matrixRow) : (float) this.energy(matrixRow , matrixCol);
			   energyMatrix[matrixRow][matrixCol] = pixelEnergy + Math.min(energyMatrix[matrixRow - 1][matrixCol - 1],
						 Math.min(energyMatrix[matrixRow - 1][matrixCol], energyMatrix[matrixRow - 1][matrixCol + 1]));
			   if(matrixRow == matrixHeight - 1 && 
					   (bestpixelColIndx == 0 || energyMatrix[matrixRow][matrixCol] < bestpixelColVal)) {
				   bestpixelColVal = energyMatrix[matrixHeight - 1][matrixCol];
				   bestpixelColIndx = matrixCol;
			   } 
		   }
	   }
	   
	   // Retrace the seam from the least energy pixel in the last pixelColumn (bestpixelRowIndx), to the start. 
	   int[] seam = new int[matrixHeight];
	   seam[matrixHeight - 1] = bestpixelColIndx;
	   int seamRow = matrixHeight - 2;
	   while(seamRow > 0) { 
		   if(seam[seamRow + 1] + 1 < matrixWidth && energyMatrix[seamRow][seam[seamRow + 1] + 1] < energyMatrix[seamRow][seam[seamRow + 1]]) {
			   if(seam[seamRow + 1] > 0 && energyMatrix[seamRow][seam[seamRow + 1] - 1] <= energyMatrix[seamRow][seam[seamRow + 1] + 1]) {
				   seam[seamRow] = seam[seamRow + 1] - 1;
			   }else {
				   seam[seamRow] = seam[seamRow + 1] + 1;
			   }
		   }else {
			   if(seam[seamRow + 1] > 0 && energyMatrix[seamRow][seam[seamRow + 1] - 1] < energyMatrix[seamRow][seam[seamRow + 1]]) {
				   seam[seamRow] = seam[seamRow + 1] - 1;
			   }else {
				   seam[seamRow] = seam[seamRow + 1];
			   }
		   }
		   seamRow--;
		   if(seamRow == 0) {
			   seam[0] = seam[1];
		   }
	   }
	   isEnergyCalculated = true;
	   return seam;
   }
 
   /**
    * Finding sequence of indices for horizontal seam. Calls generalized findSeam method.
    *
    * @return horizontal seam with the least energy
    */
   public int[] findHorizontalSeam(){
	   if(currentPictureHeight == 1 || currentPictureHeight==2 || currentPictureWidth==1 || currentPictureWidth==2) {
		   return new int[currentPictureWidth]; 
	   }else { 
		   if(horEnergyMatrix == null) {
			   horEnergyMatrix = new float[currentPictureWidth][currentPictureHeight];
		   }
		   return findSeam(lastHorizontalSeamRemoved , horEnergyMatrix , currentPictureHeight , currentPictureWidth , true);
	   }
   }
   
   /**
    * Finding sequence of indices for vertical seam. Calls generalized findSeam method.
    *
    * @return vertical seam with the least energy
    */
   public int[] findVerticalSeam() {
	   if(currentPictureHeight == 1 || currentPictureHeight==2 || currentPictureWidth==1 || currentPictureWidth==2) {
		   return new int[currentPictureHeight];
	   }else {
		   if(verEnergyMatrix == null) {
			   verEnergyMatrix = new float[currentPictureHeight][currentPictureWidth];
		   }
		   return findSeam(lastVerticalSeamRemoved , verEnergyMatrix , currentPictureWidth , currentPictureHeight , false);
	   }
   }

   /**
    *  Removes horizontal seam from current picture
    *  Updates just the helper matrices (energyMatrix and pixelsMatrix).Removal is done just by shifting values after 
    *  the removed pixel. Pixels before it are not touched and pixels after the new currentPictureHeight value are 
    *  ignored from now on, instead of reinitializing the whole matrix to new size.
    *  
    * @param seam horizontal seam to remove from picture
    */
   public void removeHorizontalSeam(int[] seam) {
	   if(seam == null || currentPictureHeight <= 1 || seam.length != currentPictureWidth) throw new IllegalArgumentException(); 
	   for(int pixelCol = 0 ; pixelCol < currentPictureWidth ; pixelCol++) {
		   
		   // pixelRow distance between removed pixels in two consecutive pixelColumns is too big (they are not adjacent).
		   if((pixelCol > 0 && Math.abs(seam[pixelCol] - seam[pixelCol - 1]) > 1) || seam[pixelCol]<0 || 
				   seam[pixelCol]>currentPictureHeight-1) {
			   throw new IllegalArgumentException();
		   }
		   
		   int shift = 1;
		   for(int pixelRow = seam[pixelCol] + shift ; pixelRow < currentPictureHeight ; pixelRow++) {
			   pixelsMatrix[pixelCol][pixelRow-shift] = pixelsMatrix[pixelCol][pixelRow];
			   if(isEnergyCalculated) {
				   horEnergyMatrix[pixelCol][pixelRow-shift]=horEnergyMatrix[pixelCol][pixelRow];
			   }
		   }
	   }
	   currentPictureHeight--;
	   lastHorizontalSeamRemoved = seam;
	   pictureObjIsValid = false;
   }

   /**
    *  Removes vertical seam from current picture.
    *  
    * @param seam vertical seam to remove from picture
    */
   public void removeVerticalSeam(int[] seam) {
	   if(seam == null || currentPictureWidth <= 1 || seam.length != currentPictureHeight) throw new IllegalArgumentException(); 
	   for(int pixelRow = 0 ; pixelRow < currentPictureHeight ; pixelRow++) {
		   if((pixelRow > 0 && Math.abs(seam[pixelRow] - seam[pixelRow - 1]) > 1) || seam[pixelRow]<0 ||
				   seam[pixelRow]>currentPictureWidth-1) throw new IllegalArgumentException();
		   int shift = 1;
		   for(int pixelCol = seam[pixelRow] + shift ; pixelCol < currentPictureWidth ; pixelCol++) {
			   pixelsMatrix[pixelCol-shift][pixelRow] = pixelsMatrix[pixelCol][pixelRow];
			   if(isEnergyCalculated) {
				   verEnergyMatrix[pixelRow][pixelCol-shift]=verEnergyMatrix[pixelRow][pixelCol];
			   }
		   }
	   }
	   
	   currentPictureWidth--;
	   lastVerticalSeamRemoved = seam;
	   pictureObjIsValid = false;
   }

   /**
    *   Main  method takes input and output file paths from arguments or defaults to input = "pic1.jpg" , output = "pic1-carved.jpg".
    *   Third argument is number of horizontal seams removed, Fourth arg is number of vertical seams, if omited defaults are  
    *   horizontal=10, vertical=10.
    *   e.g. java -cp c:\\utils\algs4.jar SeamCarver.java somePicture.jpg somePictureCarved.jpg 20 10
    *   
    *   Interesting thing to observe is how the vertical removal is slower than horizontal one. That is the case because
    *   the pixelsMatrix matrix have to be updated, and in vertical removal it has to be updated in matrixColumn*matrixRow
    *   order which is not conformant with cache-lines. That is one place in code where I couldn't make it respect cache-lines 
    *   iteration (Other solutions I can think of would just be less optimized, like not using that matrix at all).
    *
    * @param args the command-line arguments
    */
   public static void main(String[] args) {
	   String inputFile = (args.length > 0) ? args[0] : "pic1.jpg";
	   String outputFile = (args.length > 1) ? args[1] : "pic1-carved.jpg";
	   int horizontalRemoved = (args.length > 2) ? Integer.parseInt(args[2]) : 400;
	   int verticalRemoved = (args.length > 3)? Integer.parseInt(args[3]) : 50;
	   
	   Picture pic = new Picture(inputFile);
	   SeamCarver scObject = new SeamCarver(pic);
	   /*scObject.pictureData.save("pic1Experiment.jpg");
	   System.out.print("\nHorizontal seams removing :");
	   for(int x = 0 ; x < horizontalRemoved ; x++) {
		   System.out.print(horizontalRemoved - x + ",");
		   int[] seam = scObject.findHorizontalSeam();
		   scObject.removeHorizontalSeam(seam);
	   }
	   System.out.print("\nVertical seams removing :");
	   for(int x = 0 ; x < verticalRemoved ; x++) {
		   System.out.print(verticalRemoved - x + ",");
		   int[] seam = scObject.findVerticalSeam();
		   scObject.removeVerticalSeam(seam);
	   }
	   scObject.picture().save(outputFile);
	   System.out.println("\nEnd.\nPicture saved as " + outputFile);*/
   }
}
