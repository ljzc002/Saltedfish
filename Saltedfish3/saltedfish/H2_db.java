package saltedfish;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

/**
 * 以嵌入式(本地)连接方式连接H2数据库

　　这种连接方式默认情况下只允许有一个客户端连接到H2数据库
，有客户端连接到H2数据库之后，此时数据库文件就会被锁定，那么其他客户端就无法再连接了。
 */

public class H2_db {
	//public static final String str_h2path="jdbc:h2:D:/JavaDocument/h2-2017-06-10/";
	//public static final String str_h2path="jdbc:h2:tcp://127.0.0.1/D:/JavaDocument/h2-2017-06-10/";
	public static final String str_h2path="jdbc:h2:tcp://127.0.0.1/../../";
	//数据库连接URL，当前连接的是E:/H2目录下的gacl数据库
    //private static final String JDBC_URL = "jdbc:h2:D:/JavaDocument/h2-2017-06-10/saltedfish";
    private static final String JDBC_URL = "jdbc:h2:tcp://127.0.0.1/../../saltedfish";
    
    //连接数据库时使用的用户名
    private static final String USER = "saltedfish";
    //连接数据库时使用的密码
    private static final String PASSWORD = "saltedfish";
    //连接H2数据库时使用的驱动类，org.h2.Driver这个类是由H2数据库自己提供的，在H2数据库的jar包中可以找到
    private static final String DRIVER_CLASS="org.h2.Driver";
    
    public Connection conn;
    public Statement stmt;
    
    public H2_db() throws Exception
    {
    	 // 加载H2数据库驱动
        Class.forName(DRIVER_CLASS);
        // 根据连接URL，用户名，密码获取数据库连接
        this.conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
        //this.stmt=conn.createStatement();
    }
    public void CloseConn() throws Exception
    {
    	 //释放资源
        //stmt.close();
        //关闭连接
        conn.close();
    }
}
