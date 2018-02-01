package test7;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Scale;

public class BarUpdater extends Thread{
	protected int delay;
    protected Display display;
    private Scale bar;
    public Win_sf window1;
    public BarUpdater(Display display,Scale bar,Win_sf window1) {
        this.display = display;
        this.bar=bar;
        this.window1=window1;
    }
    public void run() {
        try {
            while (true) 
            {
                try 
                {
                    if (!display.isDisposed()) //这个线程一直活着，只要display不被释放就一直工作，进程结束时终结这个线程？
                    {
                        if (window1.str_currentfile !=null) {
                            //Thread.sleep(delay);
                            if (!display.isDisposed()) {
                                display.syncExec(new Runnable() 
                                {
                                    public void run() {
                                        if (!bar.isDisposed()) 
                                        {
                                            int t = (int)( window1.mediaPlayer.getTime()/1000+0.4);
                                  
                                            if (t > bar.getMaximum()) 
                                            {
                                                t = bar.getMinimum();
                                            }
                                            if(t>-1)//当v和现有播放位置差别很大时，认为不可能是由正常播放造成的变化，这时要以手动拖拽为主
                                            {
                                            	if(Math.abs(t-bar.getSelection())<2)//假设正常播放时一次检查的时间在一秒以内
                                            	{
                                            		bar.setSelection(t);
                                                    window1.lab_currenttime.setText(String.valueOf(t));
                                                    System.out.print(t);
                                            	}
                                            	else//如果变化很大就改变时间
                                            	{
                                            		if(Math.abs(bar.getSelection()-bar.getMaximum())<2&&t==0)//播放和同步是两个线程，可能在想要同步时播放已经重置了
                                            		{//播放完毕之后t会自动跳到0处
                                            			bar.setSelection(0);
                                                        window1.lab_currenttime.setText(String.valueOf(0));
                                            		}
                                            		else
                                            		{
                                            			window1.mediaPlayer.setTime((long)bar.getSelection()*1000);
                                                		window1.lab_currenttime.setText(String.valueOf(bar.getSelection()));
                                            		}
                                            		
                                            	}
                                            }                                                    
                                        }
                                    }
                                });
                            }
                        }
                    }
                    Thread.sleep(100);//每个循环体内都有一个休眠100ms
                }
                catch (InterruptedException ie) {
                }
            }
        }
        catch (Exception e) {
             e.printStackTrace();
        }
    }
}
