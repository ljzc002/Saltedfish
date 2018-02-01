package test7;
//通过反射添加事件回调
import java.lang.reflect.Method;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.MessageBox;

public class Event_cb {
	protected static final Class [] args = {};	
	public static void registerCallback(final Button btn, final Object handler, final String handlerName) 
	{//还没有弄明白怎样添加参数，以及怎样把响应函数放在这里
			btn.addSelectionListener(new SelectionListener() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					try {
						Method m = handler.getClass().getMethod(handlerName, args);
						m.invoke(handler);
					}
					catch (Exception ex) {
						ex.printStackTrace();
					}
				}	
				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
					// TODO Auto-generated method stub					
				}
			});
	}
	public static void registerCallback2(final Button btn, final Object handler
			, final String handlerName,final Object[] args) 
	{
		final Class[] argsClass = new Class[args.length];
		for (int i = 0, j = args.length; i < j; i++) {  
	         argsClass[i] = args[i].getClass();  
	     } 
		btn.addSelectionListener(new SelectionListener() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				try {
					Method m = handler.getClass().getMethod(handlerName, argsClass);//根据参数类型取方法
					m.invoke(handler,args);//使用参数列表执行方法
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}	
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub					
			}
		});
	}
	
}
