package test7;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;


//saltedfish容器，一个包含了缩略图按钮，文件名，文件长度的小模块，还含有文件路径属性，点击时把文件进行切换到主屏播放
public class Comp_sf extends Composite{
	protected Win_sf window1;
	public String str_filename="";
	public String str_filepath="";
	protected int int_filelength=0;
	protected int int_starttime=0;
	protected String str_picpath="";
	protected Composite parent;
	
	public Comp_sf(Composite parent, int style,Win_sf window1,String str_filename,String str_filepath,int int_filelength,int int_starttime,String str_picpath)
	{
		super(parent, style);
		this.parent=parent;
		this.window1=window1;
		this.str_filename=str_filename;
		this.str_filepath=str_filepath;
		this.int_filelength=int_filelength;
		this.int_starttime=int_starttime;
		this.str_picpath=str_picpath;
		initGui();
	}
	//填充小模块的内部
	protected void initGui() 
	 {
		this.setSize(400, 194);//后来想了一想，还是不能用视频长度作为容器宽度，比如只有30s的视频怎么办？
		//this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));  //改为使用绝对定位
 	   Button btn_cutpic=new Button(this,SWT.FLAT);
 	   btn_cutpic.setBounds(0,0,258, 194);
 	   Image img=new Image(window1.display, str_picpath);//如何保证这时图片生成完毕？
 	   btn_cutpic.setImage(img);
 	   //为按钮注册一个事件
 	  Object[] args=new Object[1];
 	  args[0]=str_filepath+str_filename;
 	  Event_cb.registerCallback2(btn_cutpic,this,"OpenFile",args);//打开文件
 	   //文件长度的标签
 	   Label lab_avilength=new Label(this,SWT.LEFT);	   
 	   lab_avilength.setText(String.valueOf(int_filelength));
 	   lab_avilength.setBounds(268,0,120, 100);
 	   //文件名的标签
 	   Label lab_aviname=new Label(this,SWT.LEFT);
 	   lab_aviname.setText(str_filename);
 	   lab_aviname.setBounds(268,100,120, 100);
	 }
	public void OpenFile(String str_file)
	{//点击图片按钮播放对应文件
		String str_file2=str_file.replace("/", "\\\\");//使用File.separator添加的文件直接就是\\格式的
		//window1.mediaPlayer.playMedia(str_file2);
		window1.mediaPlayer.prepareMedia(str_file2);//预先准备而不播放
		window1.str_currentfile=str_file2;
		window1.scale2.setMaximum(int_filelength/1000);
		window1.scale2.setSelection(0);
		window1.lab_endtime.setText(String.valueOf(int_filelength/1000));
		window1.int_filelength=int_filelength;
		//window1.int_palyflag=1;
		//window1.mediaPlayer.
		window1.list_sf=new ArrayList<Obj_sf>();//初始化旁白列表
		//改变被选中的btn的背景
    	Control[] arr_btn=window1.comp_down_video.getChildren();
    	int len=arr_btn.length;
    	Color white = window1.display.getSystemColor(SWT.COLOR_WHITE);
		 Color blue = window1.display.getSystemColor(SWT.COLOR_BLUE);
    	for(int i=0;i<len;i++)
    	{
    		Comp_sf comp=(Comp_sf)arr_btn[i];
    		comp.setBackground(white);
    		
    	}
    	this.setBackground(blue);
	}
}
