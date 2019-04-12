package com.example.inspiron.phonesavior.Service;

import android.app.Service;
import android.content.Context;
import android.graphics.*;
import android.hardware.Camera;
import android.media.FaceDetector;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.example.inspiron.phonesavior.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MyWindow extends LinearLayout implements SurfaceTextureListener {

	private TextureView textureView;

	/**
	 * 相机类
	 */
	private Camera myCamera;
	private Context context;

	private WindowManager mWindowManager;
    private int num = 0;
    private int curnum = 0;
    private Bitmap bitmap_get = null;
    private int count = 0;

	public MyWindow(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.window, this);
		this.context = context;
		
		initView();
	}

	private void initView() {

		textureView = (TextureView) findViewById(R.id.textureView);
		textureView.setSurfaceTextureListener(this);
		mWindowManager = (WindowManager) context.getSystemService(Service.WINDOW_SERVICE);
	}

	@Override
	public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

		if (myCamera == null) {
			// 创建Camera实例
			//尝试开启前置摄像头
			Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
			for (int camIdx = 0, cameraCount = Camera.getNumberOfCameras(); camIdx < cameraCount; camIdx++) {
				Camera.getCameraInfo(camIdx, cameraInfo);
				if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
					try {
						Log.d("Demo", "tryToOpenCamera");
						myCamera = Camera.open(camIdx);
					} catch (RuntimeException e) {
						e.printStackTrace();
					}
				}
			}

			try {
				// 设置预览在textureView上
				myCamera.setPreviewTexture(surface);
				myCamera.setDisplayOrientation(SetDegree(MyWindow.this));

				// 开始预览
				myCamera.startPreview();
                handler.sendEmptyMessage(BUFFERTAG);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

    private void getPreViewImage() {

        if (myCamera != null){
            myCamera.setPreviewCallback(new Camera.PreviewCallback(){

                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    Camera.Size size = camera.getParameters().getPreviewSize();
                    try{
                        YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
                        if(image!=null){
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            image.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, stream);

                            bitmap_get = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());

                            //**********************
                            //因为图片会放生旋转，因此要对图片进行旋转到和手机在一个方向上
                            bitmap_get = rotateMyBitmap(bitmap_get);
                            //**********************************

                            stream.close();


                        }
                    }catch(Exception ex){
                        Log.e("Sys","Error:"+ex.getMessage());
                    }
                }


            });
        }


    }

    private void myFace(Bitmap bitmap) {
        bitmap = bitmap.copy(Bitmap.Config.RGB_565, true);
        //假设最多有1张脸
        int MAXfFaces = 1;
        int numOfFaces = 0;
        FaceDetector mFaceDetector = new FaceDetector(bitmap.getWidth(),bitmap.getHeight(),MAXfFaces);
        FaceDetector.Face[] mFace = new FaceDetector.Face[MAXfFaces];
        //获取实际上有多少张脸
        numOfFaces = mFaceDetector.findFaces(bitmap, mFace);
        Log.v("------------->",  "pic num:" + num + "  face num:"+numOfFaces +" count:"+count);
        if(numOfFaces == 1 && num!=curnum){
            count++;
            curnum = num;
            Log.d("pic num:" + num,  "  eyesDistance:"+ mFace[0].eyesDistance() +"  confidence:"+ mFace[0].confidence());
        }
    }

    public Bitmap rotateMyBitmap(Bitmap mybmp){
        //*****旋转一下
        Matrix matrix = new Matrix();
        matrix.postRotate(270);

        Bitmap bitmap = Bitmap.createBitmap(mybmp.getWidth(), mybmp.getHeight(), Bitmap.Config.ARGB_8888);

        Bitmap nbmp2 = Bitmap.createBitmap(mybmp, 0,0, mybmp.getWidth(),  mybmp.getHeight(), matrix, true);

        saveImage(nbmp2);
        return nbmp2;
    };

    public void saveImage(Bitmap bmp) {
        myFace(bmp);
        /*String fileName ="Camera"+ num +".jpg";
        File file = new File(getExternalStorageDirectory(), fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    public static final int BUFFERTAG = 100;
    public static final int BUFFERTAG1 = 101;
    private boolean isGetBuffer = true;

    Handler handler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch(msg.what){

                case BUFFERTAG:
                    if(count > 60){         //十分钟提示
                        count = 0;
                        Toast.makeText(context.getApplicationContext(), "检测到您持续用眼，请注意用眼", Toast.LENGTH_SHORT).show();
                    }else {

                    }
                    if(isGetBuffer){
                        num++;
                        getPreViewImage();

                        handler.sendEmptyMessageDelayed(BUFFERTAG1, 3000);

                    }else{
                        myCamera.setPreviewCallback(null);
                    }

                    break;
                case BUFFERTAG1:
                    myCamera.setPreviewCallback(null);
                    handler.sendEmptyMessageDelayed(BUFFERTAG, 5000);
                    break ;


            }

        };


    };

    Runnable runnable=new Runnable(){
        @Override
        public void run() {
            // TODO Auto-generated method stub  
            //要做的事情，这里再次调用此Runnable对象，以实现每两秒实现一次的定时器操作  

            handler.postDelayed(this, 10000);
            Log.d("test", "running!!!");
        }
    };

	private int SetDegree(MyWindow myWindow) { 
		// 获得手机的方向
		int rotation = mWindowManager.getDefaultDisplay().getRotation();
		int degree = 0;
		// 根据手机的方向计算相机预览画面应该选择的角度
		switch (rotation) {
		case Surface.ROTATION_0:
			degree = 90;
			break;
		case Surface.ROTATION_90:
			degree = 0;
			break;
		case Surface.ROTATION_180:
			degree = 270;
			break;
		case Surface.ROTATION_270:
			degree = 180;
			break;
		}
		return degree;
	}

	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
		myCamera.stopPreview(); //停止预览
		myCamera.release();     // 释放相机资源
		myCamera = null;

		return false;
	}

	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

	}

	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surface) {

	}

}
