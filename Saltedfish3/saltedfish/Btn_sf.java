package saltedfish;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

//专用于字幕匹配的按钮，其内部以纵列形式显示每一行字幕，播放时随进度切换焦点，点击按钮时将焦点移到这里，并修改响应属性
public class Btn_sf extends Button{
	protected int int_starttime=0;// 这三个值在实际阅读时进行赋值
	protected int int_endtime=0;
	protected int int_alength=0;//实际阅读所花的时间
	protected int int_colindex=0;//按钮的索引，用来计算滚动条位置，从零开始
	public String str_zm="";
	
	public Btn_sf(Composite parent)
	{
		super(parent,SWT.PUSH|SWT.WRAP|SWT.TOP);
		checkSubclass();
	}
	//为了解决shell不能被继承的问题
	 protected void checkSubclass(){  }
}
