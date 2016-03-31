public class CropPhoto extends SherlockFragmentActivity implements OnTouchListener   {

	   private static final String TAG = "Touch";
	   // These matrices will be used to move and zoom image
	   Matrix matrix = new Matrix();
	   Matrix savedMatrix = new Matrix();
	   Boolean Rotate = false;
	   PointF image_center;
	   DialogFragment newFragment;
	   
	   int rotateDeg = 0;
	   
	   // We can be in one of these 3 states
	   static final int NONE = 0;
	   static final int DRAG = 1;
	   static final int ZOOM = 2;

	   float scale;
	   
	   int mode = NONE;
	   int Rotation_Total = 0;

	   float d, newRot ,RotateHolderPosX, RotateHolderPosY, PreRotateHeight, PreRotateWidth;
	   // Remember some things for zooming
	   PointF start = new PointF();
	   PointF mid = new PointF();
	   float oldDist = 1f;
	   float lastEvent[];
	   File imgFile;
	   
	   Button CropBtn, RotateBtn;
	   float[] values;
	   
	   Float NewX, NewY,HolderXPos, HolderYPos,HolderW,HolderH,CurrentX,CurrentY,CurrentH,CurrentW, imageH, imageW, Scale, Dims;
	   RelativeLayout RLayout, TopLayout,HolderLayout;
	   Bitmap myBitmap;
	ImageView CropImg;

	
    @Override
    protected void onDestroy() {
    super.onDestroy();

    unbindDrawables(findViewById(R.id.UserImage));
    System.gc();
    }

    private void unbindDrawables(View view) {
        if (view.getBackground() != null) {
        view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
            unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
        ((ViewGroup) view).removeAllViews();
        }
    }

	void showLoading() {
	    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
	    newFragment = MyDialogFragment.newInstance();
	    newFragment.show(ft, "dialog");
	}
	
	void hideLoading() {

	        newFragment.dismiss();
	 
	}
	

	public static class MyDialogFragment extends DialogFragment {

	    static MyDialogFragment newInstance() {
	        MyDialogFragment f = new MyDialogFragment();
	        return f;
	    }


		@Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	        Bundle savedInstanceState) {
	        View v = inflater.inflate(R.layout.loading, container, false);
	        return v;
	    }

	}
    
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cropper);
		
		CropImg = (ImageView) findViewById(R.id.UserImage);


	       SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	       String Photo = sharedPreferences.getString("Photo", null);
		

	       imgFile = new  File(Photo);

	       if(imgFile.exists()){
	    	   
	    	   
	  		 Float NewScale = (float) 0.33;
			 
	         myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
	         matrix.postScale(NewScale, NewScale);
	         myBitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true);
	         CropImg.setImageBitmap(myBitmap);
	         imageH = (float) myBitmap.getHeight();
	         imageW = (float) myBitmap.getWidth();

	    	  
	       }
	       
	       CropImg.setOnTouchListener(this);
	       
           matrix.set(savedMatrix);

           
           values = new float[9];
           matrix.getValues(values);   
           updateSizeInfo();
           FixPosition(matrix);
           
	       
		
	       CropBtn = (Button) findViewById(R.id.CropBtn);
	       RotateBtn = (Button) findViewById(R.id.RotateBtn);
	       
	       RotateBtn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if(Rotate.equals(true)){
						Rotate = false;
					} else {
						Rotate = true;
				           values = new float[9];
				           matrix.getValues(values);
				        
				      
						
						if(Math.abs(values[matrix.MSCALE_Y]) != 0){
							PreRotateHeight = Math.abs(values[matrix.MSCALE_Y])*imageH;
						}
						if(Math.abs(values[matrix.MSCALE_X]) != 0){
						
							PreRotateWidth = Math.abs(values[matrix.MSCALE_X])*imageW;
						}
					}
					
					
				}
				
	       });
	       
	       CropBtn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					
			           
			           values = new float[9];
			           matrix.getValues(values);

			           
			           Scale = values[matrix.MSCALE_Y];
			           CurrentX = values[matrix.MTRANS_X];
			           CurrentY = values[matrix.MTRANS_Y];
			           
			            float[] points={0f,0f,1f,1f};
			            matrix.mapPoints(points);
			            float scaleX=points[2]-points[0];
			            float scaleY=points[3]-points[1];
			           
			            CurrentH = Math.abs(scaleY)*imageH;
			            CurrentW = Math.abs(scaleX)*imageW;
			           
					
					float CropX = 0;
					float CropY = 0;
					
					Boolean CreateIMG = true;
					
		            
		            updateSizeInfo();

	            	if(rotateDeg == 90 || rotateDeg == -270){

	    	            //Switch width and HEight because android is dumb
	    	           CurrentW = PreRotateHeight;
	    	           CurrentH = PreRotateWidth;  
	    	            
	                	HolderXPos = HolderXPos + CurrentW;

	            	} else if(rotateDeg == 180 || rotateDeg == -180){
	    	            
	    	            //Switch width and HEight because android is dumb
	    	           CurrentW = PreRotateWidth; 
	    	           CurrentH = PreRotateHeight;

	                	HolderXPos = (HolderXPos + CurrentW);         		
	                	HolderYPos = (HolderYPos + CurrentH); 
	            	} else if(rotateDeg == 270 || rotateDeg == -90){

	    	            
	    	            //Switch width and HEight because android is dumb
	    	           CurrentW = PreRotateHeight;
	    	           CurrentH = PreRotateWidth;
	 
	                	HolderYPos = (HolderYPos + CurrentH);         		
	      
	            	}

					
			           if(CurrentX > 0){
			        	   CropX = (HolderXPos - CurrentX);
			           } else {
			        	   CropX = (Math.abs(CurrentX) + HolderXPos);
			           }

			           
			           
			           if(CurrentY > 0){
			        	   CropY = (HolderYPos - CurrentY);
			           } else {
			        	   CropY = (Math.abs(CurrentY) + HolderYPos);
			           }
			           
			           
			           Dims = HolderW;
					
					// TODO Auto-generated method stub
		            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		            SharedPreferences.Editor editor = sharedPreferences.edit();
		            editor.putFloat("CropY", CropY);
		            editor.putFloat("CropX", CropX);
		            editor.putFloat("Dims", Dims);
		            editor.putFloat("IMGHeight", CurrentH);
		            editor.putFloat("IMGWidth", CurrentW);
		            editor.putFloat("CropResize", Scale);
		            editor.putBoolean("CreateIMG", CreateIMG);
		            editor.putInt("IMGRotation", rotateDeg);
		            editor.putBoolean("CropActive", false);
		            
	                unbindDrawables(findViewById(R.id.UserImage));
	                System.gc();
	                
		            
		            editor.commit();
					
					finish();
				}   
	       }); 	

  
	}
	
	

	
	
	@Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        super.onWindowFocusChanged(hasFocus);
        updateSizeInfo();
    }
	
	 private void updateSizeInfo() {
		 RLayout = (RelativeLayout) findViewById(R.id.LeftBG);
		 HolderXPos = (float) RLayout.getWidth();
		 TopLayout = (RelativeLayout) findViewById(R.id.TopBG);
		 HolderYPos = (float) TopLayout.getHeight();
		 HolderLayout = (RelativeLayout) findViewById(R.id.CropBorder);
		 HolderH = (float) HolderLayout.getHeight();
		 HolderW = (float) HolderLayout.getWidth();
	 }
	
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {

	       ImageView view = (ImageView) v;
	       view.setScaleType(ImageView.ScaleType.MATRIX);

	       float[] values = new float[9];
	       // Dump touch event to log
	       dumpEvent(event);

	       // Handle touch events here...
	       switch (event.getAction() & MotionEvent.ACTION_MASK) {
	       case MotionEvent.ACTION_DOWN: //first finger down only
	          savedMatrix.set(matrix);
	          start.set(event.getX(), event.getY());
	         // Log.d(TAG, "mode=DRAG" );
	          mode = DRAG;
	          
	          break;

	       case MotionEvent.ACTION_POINTER_DOWN:
	    	   
	    	   
	        oldDist = spacing(event);
	        if (oldDist > 10f) {
	            savedMatrix.set(matrix);
	            midPoint(mid, event);
	            mode = ZOOM;
	        }
	        lastEvent = new float[4];
	        lastEvent[0] = event.getX(0);
	        lastEvent[1] = event.getX(1);
	        lastEvent[2] = event.getY(0);
	        lastEvent[3] = event.getY(1);
	        
	        if(Rotate == true){
	        	d = rotation(event);
	        }
	        
	        
	        break;

	       case MotionEvent.ACTION_UP: //first finger lifted
	    	  FixPosition(matrix);    	  
	       case MotionEvent.ACTION_POINTER_UP: //second finger lifted
	          mode = NONE;

	          //Fix Position after zoom make sure image is not minus x or minus y

	          FixPosition(matrix);
	                     
	          break;

	       case MotionEvent.ACTION_MOVE:
	        if (mode == DRAG && Rotate == false) {
	            // ...
	            matrix.set(savedMatrix);
	            
	            NewX = event.getX() - start.x;
	            NewY = event.getY() - start.y;
	            
	            updateSizeInfo();
	            
	            values = new float[9];
	            
	            matrix.getValues(values);

	            CurrentX = values[matrix.MTRANS_X];
	            CurrentY = values[matrix.MTRANS_Y];

            	CurrentH = Math.abs(values[matrix.MSCALE_Y])*imageH;
            	CurrentW = Math.abs(values[matrix.MSCALE_X])*imageW;
	   
	            Scale = values[matrix.MSCALE_Y];
	            
	            
	            Log.d("Scale", "Scale"+Scale);
	            
            	if(rotateDeg == 90 || rotateDeg == -270){
            		
    	            RectF RecF = new RectF(); 
    	            matrix.mapRect(RecF);

    	            CurrentX = (float) RecF.left;
    	            CurrentY = (float) RecF.top;
    	            
    	            //Switch width and HEight because android is dumb
    	           CurrentW = PreRotateHeight; 
    	           CurrentH = PreRotateWidth;
    	            
                	HolderXPos = HolderXPos + CurrentW;

            	} else if(rotateDeg == 180 || rotateDeg == -180){
    	            RectF RecF = new RectF(); 
    	            matrix.mapRect(RecF);

    	            CurrentX = (float) RecF.left;
    	            CurrentY = (float) RecF.top;
    	            
    	            //Switch width and HEight because android is dumb
    	           CurrentW = PreRotateWidth; 
    	           CurrentH = PreRotateHeight;
    	            
    	           
    	           
                	HolderXPos = (HolderXPos + CurrentW);         		
                	HolderYPos = (HolderYPos + CurrentH); 
            	} else if(rotateDeg == 270 || rotateDeg == -90){
    	            RectF RecF = new RectF(); 
    	            matrix.mapRect(RecF);

    	            CurrentX = (float) RecF.left;
    	            CurrentY = (float) RecF.top;
    	            
    	            //Switch width and HEight because android is dumb
    	           CurrentW = PreRotateHeight;
    	           CurrentH = PreRotateWidth;
    	            
    	           
    	           
                	HolderYPos = (HolderYPos + CurrentH);         		
      
            	}
	            

	            Float MaxXBack, MaxYBack = 0.0f;
	            
	            MaxXBack = HolderXPos - (CurrentW - HolderW);
	            MaxYBack = HolderYPos - (CurrentH - HolderH);          

            	Log.d("Defualt", "MaxXBack"+MaxXBack);

	            
		            if(NewX > 0){
	
			            if(HolderXPos <= CurrentX + NewX){
			            	NewX = HolderXPos - CurrentX;
			            	
			            }
		            } else {
		            	if(CurrentX > 0){
			            	if(MaxXBack >= NewX + CurrentX){

			            		NewX =	MaxXBack - CurrentX;
			            	}		            	
		            	} else {
			            	if(MaxXBack >= NewX + CurrentX ){ 

			            		NewX =	MaxXBack - CurrentX;
			            	}  
		
		            	}
	
		            }
		            
		            
		            if(NewY > 0){
			            if(HolderYPos <= CurrentY + NewY){
			            	NewY =  HolderYPos - CurrentY;	
			                   		
			            }
		            } else {
		            	
		            	if(CurrentY > 0){
			            	if(MaxYBack >= NewY + CurrentY){
			            		NewY =	MaxYBack - CurrentY;
			            	}
			            	
			            	
			            	
		            	} else {
			            	if(MaxYBack >= NewY + CurrentY ){ 
			            		NewY =	MaxYBack - CurrentY;
		
			            	}  
		
		            	}
		            	
		            }
   
	            matrix.postTranslate(NewX, NewY);

	        } else if (mode == ZOOM && event.getPointerCount() == 2) {
	        	if(Rotate == false){
	            float newDist = spacing(event);
	            matrix.set(savedMatrix);
	            if (newDist > 10f) {
	                scale = newDist / oldDist;
	                
			           values = new float[9];
			           matrix.getValues(values);

		            CurrentH = values[matrix.MSCALE_Y]*imageH;
		            CurrentW = values[matrix.MSCALE_X]*imageW;
		           		            
		            updateSizeInfo();

	            	if(rotateDeg == 90 || rotateDeg == -270){

	    	            //Switch width and HEight because android is dumb
	    	           CurrentW = PreRotateHeight;
	    	           CurrentH = PreRotateWidth;  
	    	            
	                	HolderXPos = HolderXPos + CurrentW;

	            	} else if(rotateDeg == 180 || rotateDeg == -180){
	    	            
	    	            //Switch width and HEight because android is dumb
	    	           CurrentW = PreRotateWidth; 
	    	           CurrentH = PreRotateHeight;
	    	            
	    	           
	    	           
	                	HolderXPos = (HolderXPos + CurrentW);         		
	                	HolderYPos = (HolderYPos + CurrentH); 
	            	} else if(rotateDeg == 270 || rotateDeg == -90){

	    	            
	    	            //Switch width and HEight because android is dumb
	    	           CurrentW = PreRotateHeight;
	    	           CurrentH = PreRotateWidth;
	 
	                	HolderYPos = (HolderYPos + CurrentH);         		
	      
	            	}
		            

	                if(CurrentH * scale < HolderH || CurrentW * scale < HolderH){
	                	if(CurrentH * scale > CurrentW * scale){
	                		scale = HolderH / CurrentW;
	                	} else {
	                		scale = HolderH / CurrentH;
	                	}          	
	                }

	                matrix.postScale(scale, scale, mid.x, mid.y);
	                
		            float[] points={0f,0f,1f,1f};
		            matrix.mapPoints(points);
		            float scaleX=points[2]-points[0];
		            float scaleY=points[3]-points[1];
		            
		            PreRotateHeight = Math.abs(scaleY)*imageH;
		            PreRotateWidth = Math.abs(scaleX)*imageW;          
    
	            }
 
	        	}
	        	
	           if (lastEvent != null && Rotate == true) {

	                newRot = rotation(event);
	                float r = newRot - d;
	               // Log.d("Rotation", "Rotation: "+r);
	                int NewRotation = 0;
	                
	                if(r < 50 && r > 30 || r > -50 && r < -30){
	                	
		                if(r < 50 && r > 30){
		                	if(rotateDeg != 270){
			                	rotateDeg = rotateDeg + 90;
			                	//Log.d("Rotation", "RotationNOW: "+rotateDeg);
		                	} else {
		                		rotateDeg = 0;
		                	}
		                	NewRotation = 90;
		                } else if (r > -50 && r < -30){
		                	
		                	if(rotateDeg != -270){
			                	rotateDeg = rotateDeg - 90;
			                	//Log.d("Rotation", "RotationNOW: "+rotateDeg);
		                	} else {
		                		rotateDeg = 0;
		                	}
		                	NewRotation = -90;
		                }
		                
		                Rotate = false;
		                mode = NONE;

		                updateSizeInfo();
		    	           values = new float[9];

				            matrix.getValues(values);

				            CurrentX = values[matrix.MTRANS_X];
				            CurrentY = values[matrix.MTRANS_Y];

				            CurrentH = (float) Math.abs(values[matrix.MSCALE_Y]) * imageH;
				            CurrentW = (float) Math.abs(values[matrix.MSCALE_X]) * imageW;
				            
				            if(CurrentH == 0.0f){
				            	CurrentH = PreRotateHeight;
				            }
				            
				            if(CurrentW == 0.0f){
				            	CurrentW = PreRotateWidth;
				            }				            
		                
		    	            RectF RecF = new RectF(); 
		    	            matrix.mapRect(RecF);

		    	            CurrentX = (float) RecF.left;
		    	            CurrentY = (float) RecF.top;
		    	           
		    	           if(CurrentX > HolderXPos){
		    	        	   CurrentX = HolderXPos;
		    	        	   
		    	           }
		    	           
		    	           if(CurrentY > HolderYPos){
		    	        	   CurrentY = HolderYPos;
		    	           }
		                
		                Float AxisX = CurrentX + (CurrentW / 2);
		                Float AxisY = CurrentY + (CurrentH / 2);
		                
		                
		                matrix.postRotate(NewRotation, AxisX, AxisY);
		                matrix.postTranslate(CurrentX, CurrentY);

			            matrix.getValues(values);

				           Rotate = true;
	                }

	            }
	        }
	        break;

	       }         
	       // Perform the transformation
	       view.setImageMatrix(matrix); 

	       return true; // indicate event was handled

	    }
	        private float rotation(MotionEvent event) {
	        double delta_x = (event.getX(0) - event.getX(1));
	        double delta_y = (event.getY(0) - event.getY(1));
	        double radians = Math.atan2(delta_y, delta_x);

	        return (float) Math.toDegrees(radians);
	    }

	    private float spacing(MotionEvent event) {
	       float x = event.getX(0) - event.getX(1);
	       float y = event.getY(0) - event.getY(1);
	       return (float) Math.sqrt(x * x + y * y);

	    }

	    private void midPoint(PointF point, MotionEvent event) {
	       float x = event.getX(0) + event.getX(1);
	       float y = event.getY(0) + event.getY(1);
	       point.set(x/2, y/2);

	    }


	    public void FixPosition(Matrix matrix){
	    	values = new float[9];
	           matrix.getValues(values);
	           
	           

	            RectF RecF = new RectF(); 
	            matrix.mapRect(RecF);

	            CurrentX = (float) RecF.left;
	            CurrentY = (float) RecF.top;
	            
	            
	            CurrentH = (float) Math.abs(values[matrix.MSCALE_Y])*imageH;
	            CurrentW = (float) Math.abs(values[matrix.MSCALE_X])*imageW;
	            
	            updateSizeInfo();

            	if(rotateDeg == 90 || rotateDeg == -270){

    	            //Switch width and HEight because android is dumb
    	           CurrentW = PreRotateHeight;
    	           CurrentH = PreRotateWidth;  
    	            
                	HolderXPos = HolderXPos + CurrentW;

            	} else if(rotateDeg == 180 || rotateDeg == -180){
    	            
    	            //Switch width and HEight because android is dumb
    	           CurrentW = PreRotateWidth; 
    	           CurrentH = PreRotateHeight;

                	HolderXPos = (HolderXPos + CurrentW);         		
                	HolderYPos = (HolderYPos + CurrentH); 
            	} else if(rotateDeg == 270 || rotateDeg == -90){

    	            
    	            //Switch width and HEight because android is dumb
    	           CurrentW = PreRotateHeight;
    	           CurrentH = PreRotateWidth;
 
                	HolderYPos = (HolderYPos + CurrentH);         		
      
            	}
	            
	         //   Log.d("Unfixed", "CurrentH"+CurrentH);
	        //    Log.d("Unfixed", "CurrentW"+CurrentW);
	            
	        //    Log.d("Unfixed", "CurrentX"+CurrentX);
	        //    Log.d("Unfixed", "CurrentY"+CurrentY);
	            
	        //    Log.d("Unfixed", "HolderXPos"+HolderXPos);
	         //   Log.d("Unfixed", "HolderYPos"+HolderYPos);	            
	            Float MaxXBack, MaxYBack = 0.0f;
	            
	            MaxXBack = HolderXPos - (CurrentW - HolderW);
	            MaxYBack = HolderYPos - (CurrentH - HolderH);

	                	if(HolderYPos < CurrentY || HolderXPos < CurrentX){
	                		NewX = 0.0f;
	                		NewY = 0.0f;
	                	
		    	            	if(HolderYPos < CurrentY){	
		    	            		if(CurrentY > 0){
		    	            			NewY = (float) HolderYPos - CurrentY;
		    	            		} else {
		    	            			NewY = ((float) CurrentY - HolderYPos);
		    	            		}
		    		            }
	    	            	
		    	            	if(HolderXPos < CurrentX){	
		    	            		if(CurrentX > 0){
		    	            			NewX = (float) HolderXPos - CurrentX;
		    	            		} else {
		    	            			NewX = ((float) CurrentX - HolderXPos);
		    	            		}
		    		            }
	 	    		            //matrix.set(savedMatrix); 
		    	            	
		    		//            Log.d("Fixed", "NewX"+NewX);
	    		            matrix.postTranslate(NewX, NewY);		            
	                	} else if (MaxXBack > CurrentX || MaxYBack > CurrentY){
	                		if(MaxXBack < 0){
		    	            	if(MaxXBack > CurrentX){		            	
		    		            	NewX = (float) MaxXBack + Math.abs(CurrentX);
		    		            }
	                		} else {
	                			if(CurrentX < 0){
	                				NewX = Math.abs((float) CurrentX - MaxXBack);
	                			} else {
	                				NewX = MaxXBack - CurrentX;
	                			}
	                		}
	    		            
	                		if(MaxYBack < 0){
		    	            	if(MaxYBack > CurrentY){		            	
		    		            	NewY = (float) MaxYBack + Math.abs(CurrentY);
		    		            }
	                		} else {
	                			if(CurrentX < 0){
	                				NewY = Math.abs((float) CurrentY - MaxYBack);
	                			} else {
	                				NewY = MaxYBack - CurrentY;
	                			}
	                		}
	                		
	               // 		 Log.d("Fixed", "NewX"+NewX);
	                		
	    		            matrix.postTranslate(NewX, NewY);
	                	}
	                	
	                	
	    	
	    }

	    /** Show an event in the LogCat view, for debugging */

	    private void dumpEvent(MotionEvent event) {
	       String names[] = { "DOWN" , "UP" , "MOVE" , "CANCEL" , "OUTSIDE" ,
	          "POINTER_DOWN" , "POINTER_UP" , "7?" , "8?" , "9?" };
	       StringBuilder sb = new StringBuilder();
	       int action = event.getAction();
	       int actionCode = action & MotionEvent.ACTION_MASK;
	       sb.append("event ACTION_" ).append(names[actionCode]);
	       if (actionCode == MotionEvent.ACTION_POINTER_DOWN
	             || actionCode == MotionEvent.ACTION_POINTER_UP) {
	          sb.append("(pid " ).append(
	          action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
	          sb.append(")" );
	       }

	       sb.append("[" );

	       for (int i = 0; i < event.getPointerCount(); i++) {
	          sb.append("#" ).append(i);
	          sb.append("(pid " ).append(event.getPointerId(i));
	          sb.append(")=" ).append((int) event.getX(i));
	          sb.append("," ).append((int) event.getY(i));
	          if (i + 1 < event.getPointerCount())

	             sb.append(";" );
	       }

	       sb.append("]" );
	      // Log.d(TAG, sb.toString());

	    }   
	

	
}
