package test7;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ScrollBar;

import com.jacob.activeX.ActiveXComponent;  
import com.jacob.com.ComThread;  
import com.jacob.com.Dispatch;  
import com.jacob.com.Variant; 
//控制TTS进程，要顺序的把字幕交给这个线程，并且在每一句字幕完成时要有处理
public class MSTTSSpeech1 {
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
    private Control[] arr_zm;
    private ArrayList<Obj_sf> list_sf;
    
    public MSTTSSpeech1(Win_sf window1)   
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
        this.list_sf=window1.list_sf;
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
    	
    	thread.i=window1.int_col_index;
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

    public void saveToWav2(ArrayList<Obj_sf> list,String filePath,int type)
    //type 0表示弹出文件保存窗口在指定位置生成一个wav，1表示不选择路径只在默认位置生成中间wav，2表示把中间wav和avi合并 
    {  //这里要区分阅读和保存时不同的com设置！！        
        
        /*因为没有找到ffmpeg的偏移混音方法，想办法把长度相等或略多一点的主流拿出和tts小流混音，
         * 然后重组主流*/
    	this.list_sf=list;
    	int len=list_sf.size();
        String str_ffpath=Util_File.str_ffmpegpath;
        try 
        {
        	if(ax==null)  
            {  
            	initspeak();      
            }  
	        Runtime runtime = Runtime.getRuntime();   
		     Process proce = null;
		     //提取出当前视频文件的整个音频部分
		     String cmd_onlya=str_ffpath+"ffmpeg -y -i "+window1.str_currentfile+" -map 0:a:0 "+str_ffpath+"tempa.wav";
		     proce = runtime.exec(cmd_onlya);
		     try {
				proce.waitFor();//同步等待异步线程
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		     for(int i=0;i<len;i++)
	        {
		    	 Obj_sf obj_col=list_sf.get(i);
	        	int int_starttime=obj_col.int_starttime;
	        	int int_endtime=obj_col.int_endtime;
	        	if(int_endtime==0)
	        	{
	        		continue;
	        	}
	        	// 调用输出文件流对象的打开方法，创建一个.wav文件  
	            Dispatch.call(spFileStream,"Open",new Variant(Util_File.str_ffmpegpath+"temptts.wav"),new Variant(3),new Variant(true));  
	            // 设置声音对象的音频输出流为输出文件流对象  
	            Dispatch.putRef(spVoice,"AudioOutputStream",spFileStream);  
	        	window1.lab_comment.setText("共"+len+"句，正在整理第"+(i+1)+"句"+System.currentTimeMillis());
				 System.out.println("共"+len+"句，正在整理第"+(i+1)+"句"+System.currentTimeMillis());
				 Dispatch.call(spVoice,"Speak",new Variant(obj_col.str_zm));  //这一句会引起崩溃
				 // 关闭输出文件流对象，释放资源  
			        Dispatch.call(spFileStream,"Close");  
			        Dispatch.putRef(spVoice,"AudioOutputStream",null);  //如果不关闭则文件不释放？？
				 
			        //放大TTS语音
			        String cmd_ttsl=str_ffpath+"ffmpeg -y -i "+str_ffpath+"temptts.wav -af volume=10dB -ar 48000 -ac 2 "+str_ffpath+"tempttsl.wav";
				     proce = runtime.exec(cmd_ttsl);
				     try {
						proce.waitFor();//同步等待异步线程
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
			        
				 //将音频部分分成三段，ffmpeg似乎不支持同时读一个文件三次，所以分成三步来做
			        if(int_starttime>0)
			        {
			        	String cmd_cut1=str_ffpath+"ffmpeg -y -i "+str_ffpath+"tempa.wav -t  "+int_starttime+" "+str_ffpath+"temp0.wav";
					     proce = runtime.exec(cmd_cut1);
					     try {
							proce.waitFor();//同步等待异步线程
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			        }
			     
			     String cmd_cut2=str_ffpath+"ffmpeg -y -i "+str_ffpath+"tempa.wav  -t  "+(int_endtime-int_starttime)+" -ss "+int_starttime+" "+str_ffpath+"temp1.wav";
			     proce = runtime.exec(cmd_cut2);
			     try {
					proce.waitFor();//同步等待异步线程
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			     String cmd_cut3=str_ffpath+"ffmpeg -y -i "+str_ffpath+"tempa.wav  -ss  "+int_endtime+" "+str_ffpath+"temp2.wav";
			     proce = runtime.exec(cmd_cut3);
			     try {
					proce.waitFor();//同步等待异步线程
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			     //叠加混音
			     String cmd_amix=str_ffpath+"ffmpeg -y -i "+str_ffpath+"temp1.wav  -i "+str_ffpath
			    		 +"tempttsl.wav -filter_complex amix=inputs=2:duration=longest:dropout_transition=0 "+str_ffpath+"temp1ttsl.wav";
			     //shortest,longest,first, 发现tempttsl总是比temp1长一些！！
			     proce = runtime.exec(cmd_amix);
			     try {
					proce.waitFor();//同步等待异步线程
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			   //缝合
			     String str_temp="";
			     if(int_starttime>0)
			     {
			    	 str_temp=str_ffpath+"mylist2.txt";
			     }
			     else
			     {
			    	 str_temp=str_ffpath+"mylist.txt ";
			     }
			     String cmd_sew=str_ffpath+"ffmpeg -y -f concat -safe 0 -i "+str_temp+" -c copy "+str_ffpath+"tempa.wav";
			     proce = runtime.exec(cmd_sew);//发现直接执行这个语句可以，但是通过proce执行时失败了！！相对路径问题！！！！
			     try {
					proce.waitFor();//同步等待异步线程
					/*InputStream stderr = proce.getErrorStream();//输出控制台用来测试
		            InputStreamReader isr = new InputStreamReader(stderr);
		            BufferedReader br = new BufferedReader(isr);
		            String line = null;
		            while ((line = br.readLine()) != null)
		            {            		            	
		            		System.out.println(line);
		            }		*/
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
	        }
		     //生成最终的音频
		     if(type==0)
		     {
		    	 window1.lab_comment.setText("正在复制音频文件到指定目录"+System.currentTimeMillis());
				 System.out.println("正在复制音频文件到指定目录"+System.currentTimeMillis());
			     //String cmd_replace=str_ffpath+"ffmpeg -y -i "+window1.str_currentfile+" -i "+str_ffpath+"tempa.wav -c copy -map 0:v -map 1 "+filePath+".avi";
				 String cmd_replace=str_ffpath+"ffmpeg -y -i "+str_ffpath+"tempa.wav -c copy "+filePath+".wav";
			     proce = runtime.exec(cmd_replace);
			     try {
					proce.waitFor();//同步等待异步线程
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
		     }
		     else if(type==2)
		     {
		    	 window1.lab_comment.setText("正在合并音频和视频"+System.currentTimeMillis());
				 System.out.println("正在合并音频和视频"+System.currentTimeMillis());
				 String cmd_replace=str_ffpath+"ffmpeg -y -i "+window1.str_currentfile+" -i "+str_ffpath+"tempa.wav -c copy -map 0:v -map 1 "+filePath+".avi";
			     proce = runtime.exec(cmd_replace);
			     try {
					proce.waitFor();//同步等待异步线程
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
		     }
		     proce.destroy();
		    
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}            
        
     // 关闭输出文件流对象，释放资源  
        //Dispatch.call(spFileStream,"Close");  
        //Dispatch.putRef(spVoice,"AudioOutputStream",null);  
        window1.lab_comment.setText("应该保存成功了");
        window1.int_speakflag=0;
    }
   
    public class ThreadDemo extends Thread{
    	protected Dispatch spVoice;
    	protected int flag_stop=1;//1表示正在播放
    	protected int i=0;
    	//protected String text;
    	public ThreadDemo(Dispatch spVoice,String text)
    	{
    		this.spVoice=spVoice;
    		//this.text=text;
    	}
        @Override
        public void run() 
        {        	
        			int len=list_sf.size();
		        		//while(true)//就让这个线程一直运行着吧，但是发现未知原因的不加断点就不运行情况
		        	while(flag_stop==1)	
		        	{//所以还是让这个线程能够终止吧
		        			if(i<len&&flag_stop==1)		        	
		        			{//如果不加这个限制，刚刚通过桥终止了播放，接着又由这个run给启动了
		        				final Btn_sf btn_col=(Btn_sf)arr_zm[i];
				        		window1.display.getDefault().syncExec(new Runnable()
				            	{//这是获取了主线程的操作权？其持续性导致主线程无法工作！！
				        		    public void run() 
				        		    {
				        		    	window1.lab_video_zm.setText(btn_col.str_zm);
				        		    	if(window1.btn_lastcol!=null&&window1.btn_lastcol.isDisposed()==false)//开始阅读时设置焦点
				        		    	{
				        		    		window1.btn_lastcol.setBackground(window1.white);
				        		    		window1.btn_lastcol.setForeground(window1.black);
				        		    		window1.btn_lastcol.setSelection(false);
				        		    	}
				        		    	window1.btn_lastcol=btn_col;
				        		    	window1.btn_lastcol.setBackground(window1.red);
				        		    	window1.btn_lastcol.setForeground(window1.red);
				        		    	window1.btn_lastcol.setSelection(true);
				        		    	btn_col.int_starttime=window1.scale2.getSelection();//观察它对主线程的影响，视频播放没有停止，但是main里的按钮都不能点击了！！
				        		    }
				            	});		
				        		//通过当前的进度条时间来获取时间参照
				        		//btn_col.int_starttime=window1.scale2.getSelection();//精确到秒，发现这个读取方法也是有主线程限制的！！
				        		long begintime = System.currentTimeMillis();
				            	Dispatch.call(spVoice,"Speak",new Variant(btn_col.str_zm));
				            	long endtime=System.currentTimeMillis();
				            	//在这里加一个小延迟？？
				            	try {
									this.sleep(100);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
				            	int costTime = (int) ((endtime - begintime)/1000);
				            	btn_col.int_alength=costTime;
				            	btn_col.int_endtime=btn_col.int_starttime+costTime;
				            	//考虑是否要对这个时间数据进行数据库层面的持久化，如果不持久化则半途关闭程序会丢失数据，如果持久化则 可能会影响线程流畅度
				            	//考虑增加项目层面的保存功能？或者允许加载半srt半txt的文件？？
				            	//但是完全的同步也会占去应用刷新显示的机会!!
				            	if(i<len&&flag_stop==1)	
				            	{
				            		window1.int_col_index++;//移到下一按钮	
				            	}
				            	
				            	window1.display.getDefault().asyncExec(new Runnable()
				            	
				            	{//这是获取了主线程的操作权？其持续性导致主线程无法工作！！
				        		    public void run() 
				        		    {
				        		    	ScrollBar hBar=window1.comp_zm0.getHorizontalBar();		        		   
				        		    	int i_temp=window1.int_col_index*30;			        		    				        		    	
				        		    	hBar.setSelection(i_temp);
				        		    	//滑动条确实变了，但是里面的内容没有刷新！！		  
				        		    	window1.comp_zm.setLocation(-i_temp, 0);//通过检查代码，发现滚动的本质是在改变内容的位置		        		    	
				        		    }
				            	});
				            	if(i<len&&flag_stop==1)	
				            	{
				            		i++;
				            	}
		        			}
		        			else if(i>=len)
		        			{
		        				flag_stop=0;
		        				Dispatch.putRef(spVoice,"AudioOutputStream",null);  
		        		    	//Dispatch.call(spVoice,"Release");//这个播放结束的释放交给进度条来做
		        		    	//spVoice.safeRelease();
		        				/*window1.display.getDefault().syncExec(new Runnable()				            	
				            	{//这是获取了主线程的操作权？其持续性导致主线程无法工作！！
				        		    public void run() 
				        		    {
				        		    	window1.int_palyflag=0;
				        		    	window1.mediaPlayer.stop();
				        				window1.mediaPlayer.setTime(0);
				        				window1.scale2.setSelection(0);
				        				window1.lab_currenttime.setText("0");        		    	
				        		    }
				            	});		*/        				
		        				break;
		        			}
			        	}  
		        		        	  		    
        }
    }
    
    
}
