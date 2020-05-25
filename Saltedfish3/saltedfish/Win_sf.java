package saltedfish;

import java.beans.Encoder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.*;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.binding.internal.libvlc_instance_t;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurfaceAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.linux.LinuxVideoSurfaceAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.mac.MacVideoSurfaceAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.windows.WindowsVideoSurfaceAdapter;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

//import com.sun.jna.

public class Win_sf extends Shell{
    //-start
    protected Display display;
    protected Shell shell;
    protected Menu menubar;
    protected Composite composite;
    protected SwtEmbeddedMediaPlayer mediaPlayer;
    protected int win_height=768;
    protected int win_width=1300;
    //堆栈选项卡的信息，SWT不能垂直选择元素，也不能动态添加属性，目前只能为每个要用到的对象在顶层保存指针
    protected Composite comp_stack;
    protected StackLayout stackLayout;
    protected Composite[] labels;
    protected int int_stack=0;
    protected Composite comp_up_left;
    protected Composite comp_down_video;
    protected Composite comp_down_sound1;
    protected Composite comp_down_sound2;
    protected Label lab_comment;
    private Object[] args;
    protected String xmh=null;
    private Label lab_selectedxmh;
    private Label lab_starttime;
    public Label lab_endtime;
    public Label lab_currenttime;
    public String str_currentfile=null;
    public Scale scale2;
    public int int_filelength=0;
    public int int_palyflag=0;//是否正在播放原视频
    public int int_speakflag=0;//是否正在同步播放旁白
    protected Composite comp_zm;
    protected ScrolledComposite comp_zm0;
    public int int_col_index=0;
    public MSTTSSpeech1 tts;
    public MSTTSSpeech2 tts2;
    public Label lab_video_zm;
    public Btn_sf btn_lastcol;
    public Color yellow;
    public Color white;
    public Color black;
    public Color red;
    public Text text_insert;
    public ArrayList<Obj_sf> list_sf;

    //-end
    /*	  * 主函数入口	  * */
    public static void main(String[] args)
    {
        final Display display=new Display();

        Win_sf window1 = new Win_sf(display);
        window1.layout();	//重新进行一次排版，否则复杂的窗口内容无法显示
        //检测窗口渲染状态
        while(!window1.isDisposed())
        {
            if(!display.readAndDispatch())
            {
                display.sleep();//程序运行着，但渲染休眠了
            }
        }
        display.dispose();
    }
    public Win_sf(Display display)
    {
        super(display);//父类构造
        checkSubclass();
        this.display=display;
        this.shell=this;
        this.setSize(win_width, win_height);
        this.setMinimumSize(win_width, win_height);
        //this.setBackgroundMode(SWT.INHERIT_DEFAULT); //强制继承背景色
        setLayout(new FillLayout());
        setText("Salted Fish");
        open();

        //添加退出处理
        this.addShellListener(new ShellAdapter()
        {
            public void shellClosed(ShellEvent e) {
                MessageBox mb = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
                mb.setText("Confirm Exit");
                mb.setMessage("Are you sure you want to exit?");
                int rc = mb.open();
                if(rc == SWT.OK)
                {
                    e.doit =true;
                    if(mediaPlayer != null)
                    {
                        mediaPlayer.release();
                    }
                    if(tts!=null)
                    {
                        tts.closespeak();
                    }
                    if(tts2!=null)
                    {
                        tts2.closespeak();
                    }
                    System.exit(0);//关闭JVM虚拟机？？
                }
                else
                {
                    e.doit =false;
                }
            }
        });

        list_sf=new ArrayList<Obj_sf>();
        CreateMenuBar();
        CreateComposite();
    }
    //为了解决shell不能被继承的问题
    protected void checkSubclass(){  }
    //省略窗口上部的菜单条
    private void CreateMenuBar()	 {}
    //建立窗口主体空间
    private void CreateComposite()
    {
        Composite composite=new Composite(this,SWT.NONE);//主体空间中首先要有一个填满空间的容器
        this.composite=composite;
        //composite.setLayout(new FillLayout(SWT.VERTICAL));//能够遗传给内部元素吗？
        //实验证明不遗传，并且设置了它之后会强行取代设置的具体位置和尺寸
        initGui();
    }
    //分割主窗体
    protected void initGui()
    {
        //-start
        Composite comp_up=new Composite(composite,SWT.BORDER);
        comp_up.setBounds(0,0, win_width, 400);
        //左上部放置主播放器
        this.comp_up_left=new Composite(comp_up,SWT.BORDER);
        comp_up_left.setBounds(0,0, 600, 400);
        //Composite comp_vlc=new Composite(comp_up_left,SWT.BORDER);
        //comp_vlc.setBounds(0,0, 600, 400);//默认情况下视频的宽高比会不随它改变，二者出现明显不重合
        this.black = display.getSystemColor(SWT.COLOR_BLACK);
        this.white = display.getSystemColor(SWT.COLOR_WHITE);
        //Color blue = display.getSystemColor(SWT.COLOR_BLUE);
        //Color blue = display.getSystemColor(SWT.COLOR_BLUE);
        this.yellow = new Color(display, 255, 255, 0);  //自定义颜色
        this.red = new Color(display, 255, 0, 0);
        comp_up_left.setBackground(black);
        Font font = new Font(display, "Arial", 14, SWT.BOLD | SWT.ITALIC);
        //如果希望这个label一直保持在画面之上，可以尝试把label放在一个comp之中然后使用堆栈型布局
        //SWT的基础方法里只支持继承背景色而不支持透明！！
        this.lab_video_zm=new Label(comp_up_left,SWT.CENTER|SWT.INHERIT_DEFAULT);
        lab_video_zm.setForeground(yellow);
        lab_video_zm.setFont(font);
        lab_video_zm.setBounds(0, 370, 600, 30);
        lab_video_zm.setBackground(black);
        // lab_video_zm.setText("显示字幕");

        //右上部放置主选项卡
        Composite comp_up_right=new Composite(comp_up,SWT.BORDER);
        comp_up_right.setBounds(600,0, win_width-600, 400);
        //选项卡上面的按钮区，点击不同的按钮切换不同的堆栈面板
        Composite comp_stackbtns=new Composite(comp_up_right,SWT.BORDER);
        comp_stackbtns.setBounds(0,0, 700, 30);
        Button btn_stack1=new Button(comp_stackbtns,SWT.PUSH);
        btn_stack1.setText("暂停式处理");
        Event_cb.registerCallback(btn_stack1,this,"ChangeStack1");
        btn_stack1.setBounds(0,0, 100, 26);
        Button btn_stack2=new Button(comp_stackbtns,SWT.PUSH);
        btn_stack2.setText("处理这一段");
        Event_cb.registerCallback(btn_stack2,this,"ChangeStack2");
        btn_stack2.setBounds(110,0, 100,26);
        Button btn_stack3=new Button(comp_stackbtns,SWT.PUSH);
        btn_stack3.setText("项目操作");
        Event_cb.registerCallback(btn_stack3,this,"ChangeStack3");
        btn_stack3.setBounds(220,0, 100, 26);
        //-end

        //堆栈面板
        //-start
        this.stackLayout = new StackLayout();
        this.comp_stack=new Composite(comp_up_right,SWT.BORDER);
        comp_stack.setLayout(stackLayout);
        comp_stack.setBounds(0,30, 700, 370);
        int top=20,margin_top=10,margin_left=10;
        this.labels = new Composite[5];
        for (int i = 0; i < labels.length; i++) {
            Composite xlabel = new Composite(comp_stack,SWT.NONE);
            //xlabel.setBackground(black);
            //Label型对象无法添加内部元素，Composite型对象则无法直接设置字符内容
            Label lab_temp=new Label(xlabel,SWT.CENTER);
            lab_temp.setBounds(0, 0, 100, top);
            lab_temp.setText("Stack " + i);
            labels[i] = xlabel;
        }
        //-end
        //为每一个堆栈面板添加内容
        //Combo com_xm = new Combo(labels[2], SWT.DROP_DOWN | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        //第一个堆栈面板放置单个文件播放控制和切分、序号修改

        //第二个堆栈面板里放置旁白进度对应？
        //-start
        //滑动这个滑动条调整文件播放进度
        this.scale2=new Scale(labels[1],SWT.HORIZONTAL);
        scale2.setBounds(10, 20, 600, 40);
        scale2.setMinimum(0);
        scale2.setMaximum(3600);
        scale2.addSelectionListener(
                new SelectionAdapter()
                {
                    public void widgetSelected(SelectionEvent e)
                    {

                        display.getDefault().syncExec(new Runnable() {
                            public void run() {
                                int t=scale2.getSelection();
                                //if(t==int_filelength)//进度条走到最后则停止播放




                                mediaPlayer.setTime((long)t*1000);
                                lab_currenttime.setText(String.valueOf(t));
                                if(int_palyflag==0)
                                {
                                    int_palyflag=1;
                                    mediaPlayer.play();
                                    tts.startspeak();
                                }


                            }
                        });
                        //mediaPlayer.setTime((long)t*1000);//vlcj的时间单位是ms
                        //lab_currenttime.setText(String.valueOf(t));
                    }
                }
        );
        this.lab_starttime=new Label(labels[1],SWT.NONE|SWT.LEFT);
        lab_starttime.setBounds(10, 70, 100, 20);
        lab_starttime.setText("0");
        this.lab_endtime=new Label(labels[1],SWT.NONE|SWT.RIGHT);
        lab_endtime.setBounds(510, 70, 100, 20);
        lab_endtime.setText("0");
        this.lab_currenttime=new Label(labels[1],SWT.NONE|SWT.CENTER);
        lab_currenttime.setBounds(260, 70, 100, 20);
        lab_currenttime.setText("0");
        //播放控制按钮
        Button btn_playfile=new Button(labels[1],SWT.PUSH);
        btn_playfile.setBounds(620, 10, 70, 20);
        btn_playfile.setText("播放");
        Event_cb.registerCallback(btn_playfile,this,"PlayFile");
        Button btn_pausefile=new Button(labels[1],SWT.PUSH);
        btn_pausefile.setBounds(620, 40, 70, 20);
        btn_pausefile.setText("暂停");
        Event_cb.registerCallback(btn_pausefile,this,"PauseFile");
        Button btn_stopfile=new Button(labels[1],SWT.PUSH);
        btn_stopfile.setBounds(620, 70, 70, 20);
        btn_stopfile.setText("停止");
        Event_cb.registerCallback(btn_stopfile,this,"StopFile");

        this.text_insert=new Text(labels[1],SWT.LEFT|SWT.BORDER);
        text_insert.setTextLimit(100);
        text_insert.setToolTipText("在这里输入要插入的旁白");
        text_insert.setBounds(10, 100, 600, 100);
        Button btn_read=new Button(labels[1],SWT.PUSH);
        btn_read.setBounds(620, 100, 70, 20);
        btn_read.setText("插入");
        Event_cb.registerCallback(btn_read,this,"InsertTxt");
        Button btn_rollback=new Button(labels[1],SWT.PUSH);//删除最近添加的一句旁白
        btn_rollback.setBounds(620, 150, 70, 20);
        btn_rollback.setText("回退");
        Event_cb.registerCallback(btn_rollback,this,"RollBack");
        /*
         * 使用尽量简化的配置，在点击插入时直接等长替换原文件的的音频，在播放完旁白后直接写srt文件
         *
         * */
        Button btn_srt=new Button(labels[1],SWT.PUSH);
        btn_srt.setBounds(620, 230, 70, 20);
        btn_srt.setText("srt");
        Event_cb.registerCallback(btn_srt,this,"ExportSrt");
        Button btn_wav=new Button(labels[1],SWT.PUSH);
        btn_wav.setBounds(620, 260, 70, 20);
        btn_wav.setText("wav");
        Event_cb.registerCallback(btn_wav,this,"ExportWav");
        Button btn_combine=new Button(labels[1],SWT.PUSH);
        btn_combine.setBounds(620, 290, 70, 20);
        btn_combine.setText("合并");
        Event_cb.registerCallback(btn_combine,this,"Combine");


        //第三个堆栈面板里放置和项目选取有关信息
        //-start
        //打开已有项目
        //注意swt的list容易和数据结构的list冲突！！
        org.eclipse.swt.widgets.List list_xm=new org.eclipse.swt.widgets.List(labels[2], SWT.SINGLE | SWT.V_SCROLL|SWT.BORDER);
        list_xm.setBounds(margin_left, top, 300, 100);
        String[] arr_xmh=Util_File.SearchXmh();
        setListContents(list_xm,arr_xmh);
        Button btn_s3_openxm=new Button(labels[2],SWT.PUSH);
        btn_s3_openxm.setBounds(300+margin_left*2,top, 100, 30);
        btn_s3_openxm.setText("打开项目");
        args=new Object[1];
        args[0]=list_xm;
        Event_cb.registerCallback2(btn_s3_openxm,this,"OpenXm",args);
        lab_selectedxmh=new Label(labels[2],SWT.NONE);
        lab_selectedxmh.setBounds(400+margin_left*3,top, 100, 30);
        //添加新的空项目
        int top1=top+100+margin_top;
        Text text_xmh=new Text(labels[2],SWT.LEFT|SWT.BORDER);
        text_xmh.setTextLimit(10);
        text_xmh.setToolTipText("项目id");
        text_xmh.setBounds(margin_left, top1, 100, 30);
        Text text_xmh2=new Text(labels[2],SWT.LEFT|SWT.BORDER);
        text_xmh2.setTextLimit(20);
        text_xmh2.setToolTipText("项目备注");
        text_xmh2.setBounds(margin_left*2+100,top1, 200, 30);
        Button btn_s3_createxm=new Button(labels[2],SWT.PUSH);
        btn_s3_createxm.setBounds(300+margin_left*3,top1, 100, 30);
        btn_s3_createxm.setText("新建项目");
        args=new Object[2];
        args[0]=text_xmh;
        args[1]=text_xmh2;
        Event_cb.registerCallback2(btn_s3_createxm,this,"CreateXm",args);
        //添加一个新的文件到项目，序号根据项目中已有的文件数加一生成
        int top2=top1+30+margin_top;
        Button btn_s3_addfile=new Button(labels[2],SWT.PUSH);
        btn_s3_addfile.setBounds(margin_left,top2, 100, 30);
        btn_s3_addfile.setText("添加文件");
        args=new Object[0];
        Event_cb.registerCallback2(btn_s3_addfile,this,"AddFile",args);
        //将当前列表里的文件整合成一个文件导出
        int top3=top2+30+margin_top;
        Button btn_s3_exportfile=new Button(labels[2],SWT.PUSH);
        btn_s3_exportfile.setBounds(margin_left,top3, 100, 30);
        btn_s3_exportfile.setText("导出目标文件");
        args=new Object[0];
        Event_cb.registerCallback2(btn_s3_exportfile,this,"ExportFile",args);
        //-end
        stackLayout.topControl = labels[int_stack];
        comp_stack.layout();

        //下面的可以左右拖动的资源区
        //-start
        ScrolledComposite  comp_down=new ScrolledComposite (composite,SWT.BORDER|SWT.H_SCROLL);
        comp_down.setBounds(0,400, win_width, 300);
        Composite comp_down_long=new Composite(comp_down,SWT.NONE);
        comp_down_long.setBounds(0,0, 16000, 300);//一像素一秒？
        comp_down.setContent(comp_down_long);
        this.comp_down_video=new Composite(comp_down_long,SWT.BORDER);
        comp_down_video.setBounds(0,0, 16000, 200);
        //comp_down_video.setLayout(new GridLayout(40,false));//分成五十个区域每个区域宽度300，但实际应用时后添加的单元格似乎摞在一起了
        this.comp_down_sound1=new Composite(comp_down_long,SWT.BORDER);
        comp_down_sound1.setBounds(0,200, 16000, 40);
        this.comp_down_sound2=new Composite(comp_down_long,SWT.BORDER);
        comp_down_sound2.setBounds(0,240, 16000, 40);
        //在下面放置一个提示文字
        this.lab_comment=new Label(composite,SWT.LEFT);
        lab_comment.setBounds(0, 700, win_width, 20);
        lab_comment.setText("欢迎使用！");
        //-end
        InitAssats();
    }
    //向窗体中添加资源
    protected void InitAssats()
    {
        //主播放区
        new NativeDiscovery().discover();//寻找本地vlcj库
        LibVlc libvlc = LibVlc.INSTANCE;
        libvlc_instance_t instance = libvlc.libvlc_new(0, null);
        //初始化主播放器
        SwtEmbeddedMediaPlayer mediaPlayer = new SwtEmbeddedMediaPlayer(libvlc, instance);
        this.mediaPlayer=mediaPlayer;
        mediaPlayer.setVideoSurface(new CompositeVideoSurface(comp_up_left, getVideoSurfaceAdapter()));
        mediaPlayer.addMediaPlayerEventListener(new MediaPlayerEventAdapter()
        {
            @Override
            public void positionChanged(MediaPlayer mediaPlayer, final float newPosition) {
                if(int_palyflag==1)
                {
                    //System.out.println(int_temp);
                    display.getDefault().syncExec(new Runnable() {
                        public void run() {
                            int int_temp=(int) (newPosition*(int_filelength/1000));
                            //非主线程调用由主线程管理的ui时需要这样处理！！？？
                            lab_currenttime.setText(String.valueOf(int_temp));
                            scale2.setSelection(int_temp);
                        }
                    });
                }
            }
            @Override
            public void finished(MediaPlayer mediaPlayer) {
                System.out.println("Rip completed successfully");
                int_palyflag=0;
                //mediaPlayer.stop();
                mediaPlayer.setTime(0);
                tts.closespeak();
                display.getDefault().syncExec(new Runnable()
                {//这是获取了主线程的操作权？其持续性导致主线程无法工作！！
                    public void run()
                    {
                        lab_currenttime.setText("0");
                        scale2.setSelection(0);
                    }
                });
                //看来一定要有一个stop啊，否则播放到最后程序会崩溃的。。。
            }
        });

        //初始化TTS对象
        this.tts=new MSTTSSpeech1(this);
        this.tts2=new MSTTSSpeech2(this);
    }
    //-start
    private static VideoSurfaceAdapter getVideoSurfaceAdapter()
    {
        VideoSurfaceAdapter videoSurfaceAdapter;//根据操作系统的不同加载不同的视屏对象
        if(RuntimeUtil.isNix()) {
            videoSurfaceAdapter = new LinuxVideoSurfaceAdapter();
        }
        else if(RuntimeUtil.isWindows()) {
            videoSurfaceAdapter = new WindowsVideoSurfaceAdapter();
        }
        else if(RuntimeUtil.isMac()) {
            videoSurfaceAdapter = new MacVideoSurfaceAdapter();
        }
        else {
            throw new RuntimeException("Unable to create a media player - failed to detect a supported operating system");
        }
        return videoSurfaceAdapter;
    }

    public void setListContents(List list,String[] data) {
        list.removeAll();
        for (int i = 0; i < data.length; i++) {
            list.add(data[i]);
        }
    }
    //-end
    //回调函数列表
    public void ChangeStack1()
    {
        this.stackLayout.topControl = this.labels[0];
        this.comp_stack.layout();
    }
    public void ChangeStack2()
    {
        this.stackLayout.topControl = this.labels[1];
        this.comp_stack.layout();
    }
    public void ChangeStack3()
    {
        this.stackLayout.topControl = this.labels[2];
        this.comp_stack.layout();
    }
    public void Print1(String str)
    {
        System.out.println(str);
    }
    public void OpenXm(List list_xm)
    {//返回这个项目内的各个类别的所有文件的文件列表，并且对数据库中缺少的文件长度项进行填补
        if(list_xm.getSelection().length>0)
        {
            String xmh=list_xm.getSelection()[0];
            //File[] result_avi =Util_File.SearchAvi(xmh,this);
            Util_File.SearchAvi(xmh,this);
            this.xmh=xmh;
            this.lab_selectedxmh.setText(xmh);
            this.str_currentfile=null;
        }
        else
        {
            this.lab_comment.setText("请选择一个项目");
        }
        //File[] result_audio =Util_File.SearchAudio(xmh,this);
        //MakeComplexList(result_avi,result_audio);
    }
    public void CreateXm(Text text1,Text text2)
    {
        String str1=text1.getText();
        String str2=text2.getText();
        if(str1==null||str2==null||str1.equals("")||str2.equals(""))
        {
            this.lab_comment.setText("请填写项目id和项目备注");
        }
        else
        {
            this.lab_comment.setText("开始添加项目");
            String str_res=Util_File.CreateXm(str1,str2);
            this.lab_comment.setText(str_res);
            this.str_currentfile=null;
        }
    }
    public void AddFile()
    {
        if(this.xmh==null)
        {
            this.lab_comment.setText("需要选择一个项目");
            return;
        }
        FileDialog fd = new FileDialog(this, SWT.OPEN);
        fd.setText("选择一个avi文件");
        String path = fd.open();
        System.out.println(path);
        //fd.getFileName()
        String str_filetype=path.split("\\.")[path.split("\\.").length-1];
        if(str_filetype.equals("avi"))
        {
            int i=path.lastIndexOf(File.separator);
            //path.lastIndexOf(ch)
            String str_filepath=path.substring(0,i+1);
            String str_filename=path.substring(i+1);
            this.lab_comment.setText("开始添加文件");
            String str_res=Util_File.AddFile(str_filepath,str_filename,str_filetype,this.xmh);
            this.lab_comment.setText(str_res);
            Util_File.SearchAvi(xmh,this);//重新刷新打开的项目
            this.str_currentfile=null;
        }
        else
        {
            this.lab_comment.setText("目前只支持avi打包格式");
        }
    }
    public void ExportFile()
    {
        if(this.xmh==null)
        {
            this.lab_comment.setText("需要选择一个项目");
            return;
        }
        FileDialog  dd = new FileDialog(this, SWT.NONE);
        dd.setText("选择保存路径");
        String path = dd.open();
        System.out.println(path);
        String str_res=Util_File.SaveAvi(path,this);
    }
    public void PlayFile()
    {
        try
        {
            int_palyflag=1;
            this.mediaPlayer.play();
            this.lab_comment.setText("已开始播放");
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    public void PauseFile()
    {
        if(int_speakflag==1)
        {//正在播放旁白时不能修改暂停状态
            this.lab_comment.setText("正在播放旁白时不能修改暂停状态");
        }
        else
        {
            try
            {
                if(int_palyflag==1)
                {
                    int_palyflag=0;
                    this.mediaPlayer.pause();
                    this.lab_comment.setText("已暂停播放");
                    //tts.stopspeak();
                }
                else
                {
                    int_palyflag=1;
                    this.mediaPlayer.pause();
                    this.lab_comment.setText("已开始播放");
                    //tts.startspeak();
                }

            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }

    }
    public void StopFile()
    {
        try
        {
            int_palyflag=0;
            this.mediaPlayer.stop();
            mediaPlayer.setTime(0);
            scale2.setSelection(0);	//使用反射时可以避开支线调主线ui权限的问题？？！！
            tts.closespeak();
            tts2.closespeak();
            this.lab_currenttime.setText("0");
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    public void ReadTxt()
    {
        if(this.str_currentfile==null)
        {
            this.lab_comment.setText("需要先选择一个视频文件");
            return;
        }
        if(this.int_palyflag==1)
        {
            this.lab_comment.setText("播放停止才可以选文本文件");
            return;
        }

        FileDialog fd = new FileDialog(this, SWT.OPEN);
        fd.setText("选择一个txt文件");
        String path = fd.open();
        System.out.println(path);
        String str_filetype=path.split("\\.")[path.split("\\.").length-1];
        if(str_filetype.equals("txt"))
        {
            Control[] arr_zm=comp_zm.getChildren();
            for(int i=0;i<arr_zm.length;i++)
            {
                arr_zm[i].dispose();
            }
            this.comp_zm0.setOrigin(0,0);
            FileInputStream fis=null;
            BufferedReader br=null;
            try
            {
                fis = new FileInputStream(new File(path));
                br = new BufferedReader(new InputStreamReader(fis));

                String line = null;
                int count=0;
                Font font = new Font(display, "Arial", 14, SWT.BOLD | SWT.ITALIC);
                while ((line = br.readLine()) != null)
                {
                    //this.comp_zm.setLocation(0, 0);

                    Btn_sf btn_col=new Btn_sf(this.comp_zm);//只用原生的Button就行，还是要创建一个继承？？
                    btn_col.setText(line);
                    btn_col.str_zm=line;
                    btn_col.setBounds(30*count+285, 0, 30, 280);//默认情况下这些按钮会如何排列？？它们默认会摞在一起，只能看到一个
                    btn_col.setFont(font);
                    btn_col.int_colindex=count;
                    btn_col.setBackground(white);
                    btn_col.setForeground(black);
                    args=new Object[1];
                    args[0]=btn_col;
                    Event_cb.registerCallback2(btn_col,this,"PickZm",args);
                    count++;
                }
            }catch(Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                try
                {
                    br.close();
                    fis.close();
                }catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        else
        {
            this.lab_comment.setText("目前只支持txt文本格式");
        }
    }
    //点击时同步状态，当然每次启动播放时也要同步状态
    public void PickZm(Btn_sf btn_col)
    {
        //焦点效果变换
        if(btn_lastcol!=null&&btn_lastcol.isDisposed()==false)
        {
            btn_lastcol.setBackground(white);
            btn_lastcol.setForeground(black);
            btn_lastcol.setSelection(false);
        }
        btn_lastcol=btn_col;
        btn_lastcol.setBackground(red);
        btn_lastcol.setForeground(red);
        btn_lastcol.setSelection(true);
        int_col_index=btn_col.int_colindex;
        int int_x=btn_col.int_colindex*30;
        comp_zm.setLocation(-int_x, 0);
        comp_zm0.getHorizontalBar().setSelection(int_x);
        tts.stopspeak();
        tts.startspeak();//从新的位置开始播放
        System.out.println(this.int_col_index);
    }
    //根据按钮情况生成srt文件
    public  void ExportSrt()
    {
        int len=list_sf.size();
        //int i=0;
        if(len>0)
        {
            FileDialog fd = new FileDialog(this, SWT.OPEN);
            fd.setText("保存字幕文本文件，建议保存为srt后缀");
            String path = fd.open();
            File file = new File(path);
            Util_File.saveToSrt(list_sf,file,this);
        }
        else
        {
            lab_comment.setText("没有发现要整理的字幕");
        }
        lab_comment.setText("开始整理字幕了，请不要乱动");
    }
    //根据按钮情况生成wav文件
    public void ExportWav()
    {
        if(this.int_palyflag==1)
        {
            lab_comment.setText("必须先停止播放");
            return;
        }
        int len=list_sf.size();
        int i=0;
        if(len>0)
        {
            lab_comment.setText("开始整理声音了，请不要乱动");
            int_speakflag=1;
            FileDialog fd = new FileDialog(this, SWT.OPEN);
            fd.setText("保存字幕音频");
            String path = fd.open();
            try {
                tts.saveToWav2(list_sf, path,0);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                lab_comment.setText("看来是保存失败了");
                int_speakflag=0;
            }

        }
        else
        {
            lab_comment.setText("没有发现要整理的声音");
        }

    }

    //一键插入
    public void InsertTxt()
    {
        if(this.int_palyflag==1)
        {
            lab_comment.setText("必须先暂停播放");
            return;
        }
        String str_insert=text_insert.getText();
        if(str_insert==null||str_insert.equals(""))
        {
            this.lab_comment.setText("请填写要插入的文本");

        }
        else
        {
            lab_comment.setText("开始插入旁白");
            try {
                int_speakflag=1;
                tts2.saveToWav3(str_insert,0);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                lab_comment.setText("看来是保存失败了");
            }
        }
    }
    //删除最近的一句旁白
    public void RollBack()
    {
        if(list_sf.size()>0)
        {
            list_sf.remove(list_sf.size()-1);
        }

    }

    public void Combine()
    {
        if(this.int_palyflag==1)
        {
            lab_comment.setText("必须先停止播放");
            return;
        }
        int len=list_sf.size();
        int i=0;
        if(len>0)
        {
            lab_comment.setText("开始整理声音了，请不要乱动");
            int_speakflag=1;
            FileDialog fd = new FileDialog(this, SWT.OPEN);
            fd.setText("请选择avi文件保存位置，并填写文件名");
            String path = fd.open();
            try {
                tts.saveToWav2(list_sf, path,2);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                lab_comment.setText("看来是保存失败了");
                int_speakflag=0;
            }

        }
        else
        {
            lab_comment.setText("没有发现要整理的声音");
        }
    }
}
