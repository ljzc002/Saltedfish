package test7;

import java.util.ArrayList;

public class Obj_sf {
	protected int int_starttime=0;// 这三个值在实际阅读时进行赋值
	protected int int_endtime=0;
	protected int int_alength=0;//实际阅读所花的时间
	protected int int_colindex=0;//
	public String str_zm="";
	public int int_flagalive=1;//这个字幕节点是否活着
	
	//判断一个字幕的插入是否合法，判断是否需要去掉一些被覆盖的旁白，并对所有的旁白重新排序
	public static void LetMeIn(ArrayList<Obj_sf> list_sf,Obj_sf obj)
	{
		int len=list_sf.size();
		if(len==0)
		{
			list_sf.add(obj);
		}
		else
		{
			//int i=0;
			for(int i=0;i<len;i++)
			{
				Obj_sf obj_temp=list_sf.get(i);
				if(obj_temp.int_starttime>obj.int_endtime)
				{
					list_sf.add(i, obj);
					//检查有没有重叠的旁白
					for(int j=0;j<i;j++)
					{
						Obj_sf obj_temp2=list_sf.get(j);
						if((obj_temp2.int_starttime>obj.int_starttime&&obj_temp2.int_starttime<obj.int_endtime)
								||(obj_temp2.int_endtime>obj.int_starttime&&obj_temp2.int_endtime<obj.int_endtime))
						{
							obj_temp2.int_flagalive=0;
							list_sf.remove(j);
							j--;
							i--;
						}
					}
					break;
				}
				else if(i==(len-1))
				{
					list_sf.add( obj);
					if((obj_temp.int_starttime>obj.int_starttime&&obj_temp.int_starttime<obj.int_endtime)
							||(obj_temp.int_endtime>obj.int_starttime&&obj_temp.int_endtime<obj.int_endtime))
					{
						obj_temp.int_flagalive=0;
						list_sf.remove(i);
					}
				}
			}
		}
		len=list_sf.size();
		for(int i=0;i<len;i++)
		{
			Obj_sf obj_temp=list_sf.get(i);
			obj_temp.int_colindex=i+1;
		}
	}
}
