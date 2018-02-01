package test7;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ScrollBar;

//数据库的最基本实践，没有事务，没有防注入
public class Util_File 
{
	//public static String str_ffmpegpath="d:"+File.separator+"SaltedFish"+File.separator;
	public static String str_ffmpegpath="."+File.separator;
	public static File[] SearchAvidb() //改为使用数据库来搜索
	{
		File files[] = new File[0];
		try {
			H2_db h2db=new H2_db();
			//Statement stmt = h2db.conn.createStatement();
			String sql="";
			Statement stmt =h2db.conn.prepareStatement(sql);
			
			stmt.close();
			h2db.conn.close();
			h2db=null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();//同时返回空的文件列表
		}		
		return files;
	}
	public static String[] SearchXmh() //查看数据库中所有的项目号
	{
		List<String> result = new ArrayList<String>();
		try {
			H2_db h2db=new H2_db();
			String sql="select xmh from tab_xm";
			//Statement stmt =h2db.conn.prepareStatement(sql);
			Statement stmt = h2db.conn.createStatement();
			//h2没有无参数的executeQuery方法，所以没法用prepareStatement模式
			ResultSet rs =stmt.executeQuery(sql);
			//遍历结果集
	        while (rs.next()) {
	        	result.add(rs.getString(1));
	        }
			stmt.close();
			h2db.conn.close();
			h2db=null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();//同时返回空的文件列表
		}	
		String[] arr_xmh= new String[result.size()];
		result.toArray(arr_xmh);//有必要转化为数组吗？是否可以直接返回list？
		return arr_xmh;
	}
	//查找项目包含的视频文件列表
	public static void SearchAvi(String xmh,Win_sf window1) //
	{
		List<File> result = new ArrayList<File>();
		List<List> list=new ArrayList<List>();
		List<String> list2=new ArrayList<String>();
		window1.lab_comment.setText("开始整理视频文件了，建议什么都不要动。");
		H2_db h2db=null;
		Statement stmt=null;
		int count_file=0;
		try {
			h2db=new H2_db();
			String sql="select id,filepath,filename,xh,length,starttime,pic from tab_file "
					+ "where filetype='avi' and xmh='"+xmh+"' order by xh";
			//对于新添加的文件，其length,starttime,pic项是空的
			//Statement stmt =h2db.conn.prepareStatement(sql);
			stmt = h2db.conn.createStatement();
			//h2没有无参数的executeQuery方法，所以没法用prepareStatement模式
			ResultSet rs =stmt.executeQuery(sql);
			//遍历结果集
			while(rs.next())
			{
				list2=new ArrayList<String>();
				for(int i=1;i<=rs.getMetaData().getColumnCount();i++)//rs从一开始
				{
						list2.add(rs.getString(i)); 						
				}
				list.add(list2);//list从零开始
				list2=null;					
			}	   
			//更新项目表的最近修改时间
			sql="update tab_xm set last_modified_date=sysdate where xmh='"+xmh+"'";
			int k0=stmt.executeUpdate(sql);
			//对项目中的文件的序号进行整理，整理成行号整数
			sql="update TAB_FILE t set xh=(select rownum from (select id,rownum from tab_file where xmh='"+xmh+"' order by xh) a where t.id=a.id) where xmh='"+xmh+"'";
			int k1=stmt.executeUpdate(sql);
			//清空现有的文件列表
	        	window1.mediaPlayer.stop();
	        	Composite comp_down_video=window1.comp_down_video;
	        	Control[] arr_sf=comp_down_video.getChildren();
	        	for(int i=0;i<arr_sf.length;i++)
	        	{
	        		arr_sf[i].dispose();
	        	}	       
			//首先寻找所有长度和起始时间为空的行进行补充
			int i=0;
			int starttime=0;//这个视频的接续时间
			for(i=0;i<list.size();i++)//对于从数据库查出来的每一个文件
			{
				list2=list.get(i);
				String str_name="";
				String str_path="";
				String str_pic=str_ffmpegpath+"default.jpg";
				int length_ms=0;//这段文件的长度
				if(list2.get(4)==null)//如果需要补全信息
				{//需要填充长度，根据前后xh计算开始时间，指定图片
					str_name=list2.get(2);//文件名
					str_path=list2.get(1);//文件路径
					Runtime runtime = Runtime.getRuntime();   
		            Process proce = null;  		            
		            
					String  shell_cutpic=Util_File.str_ffmpegpath+"ffmpeg -i "+str_path+str_name+" -y -f image2 -ss 1 -t 0.001 -s 256*192 "+str_path+str_name.split("\\.")[0]+".jpg";
					try {						
						proce = runtime.exec(shell_cutpic);
						str_pic=str_path+str_name.split("\\.")[0]+".jpg";						
						InputStream stderr = proce.getErrorStream();//令人始料未及的是java和ffmpeg都把输出放在了err流里，看来不同的外部程序要区别对待啊！！！！
			            InputStreamReader isr = new InputStreamReader(stderr);
			            BufferedReader br = new BufferedReader(isr);
			            String line = null;
			            while ((line = br.readLine()) != null)
			            {            	
			            	if(line.indexOf("Duration:")>0&&length_ms==0)
				            {
			            		System.out.println(line);
				            	String str_length=line.substring(line.indexOf("Duration:")+10, line.indexOf(","))+"0";
				            	Calendar c = Calendar.getInstance();
				                c.setTime(new SimpleDateFormat("HH:mm:sss").parse(str_length));
				                Calendar c2 = Calendar.getInstance();
				                c2.setTime(new SimpleDateFormat("HH:mm:sss").parse("00:00:00.000"));
				            	length_ms=(int) c.getTimeInMillis()-(int) c2.getTimeInMillis();
				            	//break;让ffmpeg日志打印完全
				            }
			            }			                
			            int exitVal = proce.waitFor();
			            System.out.println("Process exitValue: " + exitVal);			            					
				        proce.destroy();
				        //执行SQL语句把长度、起始时间、缩略图路径写回数据库
				        sql="update tab_file set length='"+length_ms+"',starttime='"+starttime+"',pic='"+str_pic+"'"
				        		+ " where id='"+list2.get(0)+"'";				        
				        int k=stmt.executeUpdate(sql);			        			        
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}  										
				}
				else
				{
					str_name=list2.get(2);//文件名
					str_path=list2.get(1);//文件路径
					length_ms=Integer.valueOf(list2.get(4));
					int starttime2=Integer.valueOf(list2.get(5));
					if(starttime2!=starttime)
					{//发现数据库中保存的开始时间与实际计算出的开始时间不一致
						sql="update tab_file set starttime="+starttime+ " where id='"+list2.get(0)+"'";				        
				        int k=stmt.executeUpdate(sql);
					}
					str_pic=list2.get(6);
				}				
				//添加小模块
				Comp_sf compsf=new Comp_sf(window1.comp_down_video,SWT.BORDER,window1
						,str_name,str_path,length_ms,starttime,str_pic);
				compsf.setLocation(400*i,0);
				starttime+=length_ms;	
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();//同时返回空的文件列表
		}	
		finally {
			try {
				stmt.close();
				h2db.conn.close();
				h2db=null;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		//File[] arr_xmh= new File[result.size()];
		window1.lab_comment.setText("视频文件整理完成，共找到"+String.valueOf(list.size())+"个视频文件。");
		//result.toArray(arr_xmh);//有必要转化为数组吗？是否可以直接返回list？
		//return arr_xmh;
	}
	//添加一个新的空项目
	public static String CreateXm(String xmh,String xmh1)
	{
		String str_res="";
		String sql="";
		H2_db h2db=null;
		Statement stmt=null;
		try {
			h2db=new H2_db();
			stmt = h2db.conn.createStatement();
				        //执行SQL语句把长度、起始时间、缩略图路径写回数据库
		    sql="insert into tab_xm values(uuid(),'"+xmh+"','"+xmh1+"',sysdate,sysdate)";	        
			int k=stmt.executeUpdate(sql);
			if(k==1)
			{
				str_res="新增项目成功";
			}
			else
			{
				str_res="新增项目失败，可能是项目id重复了";
			}			        			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();//同时返回空的文件列表
			str_res="新增项目失败，可能是项目id重复了";
		}	
		finally {
			try {
				stmt.close();
				h2db.conn.close();
				h2db=null;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		return str_res;
	}
	//为项目添加一个文件
	public static String AddFile(String str_filepath,String str_filename
			,String str_filetype,String str_xmh)
	{
		String str_res="";
		String sql="";
		H2_db h2db=null;
		Statement stmt=null;
		try {
			h2db=new H2_db();
			stmt = h2db.conn.createStatement();
				        //执行SQL语句把长度、起始时间、缩略图路径写回数据库
		    sql="insert into tab_file values(uuid(),'"+str_filepath+"','"+str_filename+"','"
				        +str_filetype+"','"+str_xmh+"',(select count(*)+1 from tab_file where xmh='"+str_xmh+"')"
				        		+ ",null,null,null)";	        
			int k=stmt.executeUpdate(sql);
			if(k==1)
			{
				str_res="新增文件成功";
				sql="update tab_xm set last_modified_date=sysdate where xmh='"+str_xmh+"'";
				k=stmt.executeUpdate(sql);
			}
			else
			{
				str_res="新增文件失败";
			}			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();//同时返回空的文件列表
			str_res="新增文件失败，注意项目里不能有同名文件";
		}	
		finally {
			try {
				stmt.close();
				h2db.conn.close();
				h2db=null;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		return str_res;
	}
	//在指定路径下保存目标文件
	public static String SaveAvi(String path,Win_sf window1)
	{
		String str_res="";
		Runtime runtime = Runtime.getRuntime();   
        Process proce = null;
        String str_avis="concat:";
        window1.mediaPlayer.stop();
    	Composite comp_down_video=window1.comp_down_video;
    	Control[] arr_sf= comp_down_video.getChildren();
    	for(int i=0;i<arr_sf.length;i++)
    	{
    		try
    		{
    			if(i==0)
        		{
        			str_avis+=(((Comp_sf)arr_sf[i]).str_filepath+((Comp_sf)arr_sf[i]).str_filename);
        			//Object obj=arr_sf[i].getData();
        		}
        		else
        		{
        			str_avis+=("|"+((Comp_sf)arr_sf[i]).str_filepath+((Comp_sf)arr_sf[i]).str_filename);
        		}
    		}
    		catch(Exception e)
    		{
    			e.printStackTrace();
    		}  		
    	}	  
    	
        String  shell_savefile=Util_File.str_ffmpegpath+"ffmpeg -y -i \""+str_avis+"\" -c copy "+path;
        try {						
			proce = runtime.exec(shell_savefile);
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return str_res;
	}	
	//将秒数转化为冒号分割的时间字符串
	public static String S2str(int int_s)
	{
		String str_res="";
		String str_h=String.valueOf((int)(int_s/3600));
		String str_min=String.valueOf((int)((int_s%3600)/60));
		String str_s=String.valueOf((int)(int_s%60));
		str_res=str_h+":"+str_min+":"+str_s+".001";		//完全是0不显示？				
		return str_res;
	}
	//根据字幕数组和文件句柄保存字幕文本
	public static void saveToSrt(Control[] arr_zm,File file,final Win_sf window1)  
    {
		try {
			file.createNewFile();
			OutputStreamWriter ow=new OutputStreamWriter (new FileOutputStream (file,true));//测试发现可以设置为UTF-8，但设为ANSI会报错！！
			//FileWriter fw = new FileWriter(file, true);//FileWriter不让改编码！！？？
			BufferedWriter bw = new BufferedWriter(ow);
			int len=arr_zm.length;
			for(int i=0;i<len;i++)
			 {//对于每一列字幕
				 Btn_sf btn_col=(Btn_sf)(arr_zm[i]);
				 if(btn_col.int_alength>0&&btn_col.int_endtime>0)
				 {	
					 bw.write(String.valueOf(i+1)+"\r\n");
					 bw.write(Util_File.S2str(btn_col.int_starttime)+"-->"+Util_File.S2str(btn_col.int_endtime)+"\r\n");
					 bw.write(btn_col.str_zm+"\r\n\r\n");
				     bw.flush();
				 }
			 }
			bw.close();
	        ow.close();
	        window1.display.getDefault().asyncExec(new Runnable()        	
        	{//这是获取了主线程的操作权？其持续性导致主线程无法工作！！
    		    public void run() 
    		    {
    		    	window1.lab_comment.setText("字幕应该保存成功了");        		    	
    		    }
        	});
	        //window1.lab_comment.setText("应该保存成功了");//静态方法的这一句不可用！！？？
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			window1.lab_comment.setText("看来字幕保存失败了");
		}	        	
    }
}

