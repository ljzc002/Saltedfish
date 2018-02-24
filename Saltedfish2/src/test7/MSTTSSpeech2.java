package test7;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ScrollBar;

import test7.MSTTSSpeech1.ThreadDemo;

import com.jacob.activeX.ActiveXComponent;  
import com.jacob.com.ComThread;  
import com.jacob.com.Dispatch;  
import com.jacob.com.Variant; 

public class MSTTSSpeech2 {
	private int volume=100;// 声音：1到100  
    private int rate=-1;// 频率：-10到10  
    private int voice=0;// 语音库序号  
    private int audio=0;// 输出设备序号  
    private ActiveXComponent ax=null;  
    private Dispatch spVoice=null;// 声音对象  
    private Dispatch spFileStream=null;// 音频文件输出流对象，在读取或保存音频文件时使用  
    private Dispatch spAudioFormat=null;// 音频格式对象  
    private Dispatch spMMAudioOut=null;// 音频输出对象  
    private int formatType=6;// 音频的输出格式，默认为：SAFT22kHz16BitMono  
    public String text="";
    //private Timer timer= new Timer();//Timer不手动关闭会导致程序退出不完全！！！！
    public ThreadDemo thread;
    private Win_sf window1;
    //private Control[] arr_zm;
    
    public MSTTSSpeech2(Win_sf window1)   
    {  
    	this.window1=window1;
        ComThread.InitSTA();  
        if(ax==null)  
        {  
        	initspeak();      
        }  
    }  
  //重新初始化com组件句柄
    public void initspeak()
    {
    	 ax=new ActiveXComponent("Sapi.SpVoice");  
         spVoice=ax.getObject();  
      // 调整音量和读的速度  
         Dispatch.put(spVoice,"Volume",new Variant(this.volume));// 设置音量  
         Dispatch.put(spVoice,"Rate",new Variant(this.rate));// 设置速率 
         ax=new ActiveXComponent("Sapi.SpAudioFormat");  
         spAudioFormat=ax.getObject();  
         Dispatch.put(spAudioFormat,"Type",new Variant(this.formatType)); 
       //音频设备输出
         ax=new ActiveXComponent("Sapi.SpMMAudioOut");  
         spMMAudioOut=ax.getObject();  
         Dispatch.putRef(spMMAudioOut,"Format",spAudioFormat);  
        // 创建输出文件流对象  
         ax=new ActiveXComponent("Sapi.SpFileStream");  
         spFileStream=ax.getObject();   
         Dispatch.putRef(spFileStream,"Format",spAudioFormat);  
         
         //Dispatch.put(spAudioFormat,"Type",new Variant(this.formatType));              
         Dispatch.put(spVoice,"AllowAudioOutputFormatChangesOnNextSet",new Variant(false));  
         this.thread=new ThreadDemo(spVoice,text);   
    }
    
  //要循环的调用speak方法
    public void startspeak()
   {
   	 if(ax==null)  
        {  
        	initspeak();      
        }  
   	// 设置声音对象的音频输出流        
       Dispatch.putRef(spVoice,"AudioOutputStream",spMMAudioOut);  
   	//this.arr_zm=window1.comp_zm.getChildren();//这里还输入主线程吗？
   	if(this.thread==null)
   	{
   		this.thread=new ThreadDemo(spVoice,text);
       	thread.start();         	
   	}
   	else
   	{
   		//thread.run();//????这种调用方法其实是单线程同步的
   		//认为这时上一个线程已经自动消亡了？
   		this.thread=new ThreadDemo(spVoice,text);//start不允许使用两次，所以重建线程
   		thread.start();  
   	}
   	thread.flag_stop=1;
   	//thread.isAlive();
   }
 //暂停这一句speak
   public void pausespeak()
   {
   	Dispatch.call(spVoice,"Pause"); 
   }
   //停止speak但不清空资源
   public void stopspeak()
   {
   	if(this.thread!=null)
   	{
   	thread.flag_stop=0;
   	Dispatch.call(spVoice,"Speak",new Variant(text),new Variant(2)); 
   	}
   }
   public void speak(String text)
   {//一句一句的阅读
   	//如果上一句还没有读完
   	//thread.text=text;
   	thread.start();
   }
 //完全停止播放并尽力释放资源
   public void closespeak()
   {
   	if(this.thread!=null)
   	{
   		thread.flag_stop=0;
	    	Dispatch.call(spVoice,"Speak",new Variant(text),new Variant(2)); 
	    	//Dispatch.call(spMMAudioOut,"Close");  
	    	Dispatch.putRef(spVoice,"AudioOutputStream",null);  
	    	//Dispatch.call(spVoice,"Release");
	    	spVoice.safeRelease();
	    	spAudioFormat.safeRelease();
	    	spMMAudioOut.safeRelease();
	    	spFileStream.safeRelease();
	    	ax=null;
   	}
   }
   
   public void saveToWav3(String str,int type)
   {
	   text=str;
	   
	   this.startspeak();
   }
   
    public class ThreadDemo extends Thread
    {
	   	protected Dispatch spVoice;
	   	protected int flag_stop=1;//1表示正在播放
	   	protected int i=0;
	   	protected int int_starttime=0;// 这三个值在实际阅读时进行赋值
		protected int int_endtime=0;
		protected int int_alength=0;//实际阅读所花的时间
		protected int int_colindex=0;//字幕的索引
		
	   	//protected String text;
	   	public ThreadDemo(Dispatch spVoice,String text)
	   	{
	   		this.spVoice=spVoice;
	   		//this.text=text;
	   	}
       @Override
       public void run() 
       {       
    	 //通过当前的进度条时间来获取时间参照
    	   
    	   window1.display.getDefault().syncExec(new Runnable()
    	   {//这是获取了主线程的操作权？其持续性导致主线程无法工作！！
				        		    public void run() 
				        		    {
				        		    	window1.lab_video_zm.setText(text);      
				        		    	int_starttime=window1.scale2.getSelection();
				        		    }
			});		
				        		
				        		window1.mediaPlayer.pause();
				        		window1.int_palyflag=1;
				        		long begintime = System.currentTimeMillis();
				            	Dispatch.call(spVoice,"Speak",new Variant(text));
				            	long endtime=System.currentTimeMillis();
				            	//在这里加一个小延迟？？
				            	try {
									this.sleep(100);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
				            	int costTime = (int) ((endtime - begintime)/1000);
				            	this.int_alength=costTime;
				            	this.int_endtime=this.int_starttime+costTime;
		        				Dispatch.putRef(spVoice,"AudioOutputStream",null);  
		        //读完之后暂停一下，把旁白替换到原文件中？	
		        //还是决定使用Obj_sf来暂存每一句旁白，然后统一导出
		        window1.mediaPlayer.pause();
		        window1.int_palyflag=0;	
		        //暂停一下做好排序工作，然后再继续
		        Obj_sf obj=new Obj_sf();
		        obj.int_alength=this.int_alength;
		        obj.int_starttime=this.int_starttime;
		        obj.int_endtime=this.int_endtime;
		        obj.int_colindex=this.int_colindex;
		        obj.str_zm=text;
		        Obj_sf.LetMeIn(window1.list_sf, obj);
		        window1.int_speakflag=0;  				
		        window1.mediaPlayer.pause();
		        window1.int_palyflag=1;	
		        //等待线程自动消亡
		        		
		        			
			        	
		        		        	  		    
       }
   }
   
}
