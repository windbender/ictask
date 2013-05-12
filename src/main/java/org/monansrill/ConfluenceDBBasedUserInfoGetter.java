package org.monansrill;
/**
 * Copyright Chris Schaefer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ConfluenceDBBasedUserInfoGetter {
	
    static Logger logger = LoggerFactory.getLogger(ConfluenceDBBasedUserInfoGetter.class);

	public static PoolingDataSource setupDataSource() {
        try {
                Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
                e.printStackTrace();
        }
        ObjectPool connectionPool = new GenericObjectPool(null);
        String connectionURI;
        Properties secretProperties = getSecretProperties();

        if(secretProperties.getProperty("dbpass") == null) {
        	System.err.println("sorry you need to specifiby dbpass and dbuser as properties with $HOME/.ictask.properties");
        	System.exit(-1);
        }
        connectionURI = "jdbc:mysql://localhost/confluence?zeroDateTimeBehavior=convertToNull";
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory( connectionURI, secretProperties.getProperty("dbuser"), secretProperties.getProperty("dbpass"));
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory( connectionFactory, connectionPool, null, null, false, true);
        PoolingDataSource dataSource = new PoolingDataSource(connectionPool);

        return dataSource;
	}
	
	public static Properties getSecretProperties() {
        String homedir = System.getProperty("user.home");
        File f = new File(homedir + File.separator+ ".ictask.properties");
        Properties props = new Properties();
        try {
                FileInputStream fis = new FileInputStream(f);
                props.load(fis);
                fis.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
        return props;
	}

	DataSource ds= setupDataSource();
	
	public UserInfo getUserInfoFor(String username) {
		String sql = "select first_name, credential, email_address from cwd_user where lower_user_name = ? limit 1";
		Connection conn = null;
		try {
			conn = ds.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1,username);
			ResultSet rs = pstmt.executeQuery();
			if(rs.next()) {
				return new UserInfo(rs.getString("first_name"),rs.getString("credential"),rs.getString("email_address"));
			}
		} catch(SQLException sqle) {
			logger.error("oh no...."+sqle);
			if(AuthServlet.debugging) {
				return new UserInfo("chris","CREDS","chrischris@1reality.org");
			}
		} finally {
			if(conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}
		return null;
	}
	
	public List<UserInfo> getUserInfoList() {
		String sql = "select first_name, credential, email_address from cwd_user";
		Connection conn = null;
		List<UserInfo> list = new ArrayList<UserInfo>();
		try {
			conn = ds.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()) {
				String username = rs.getString("first_name");
				if(username != null) {
					list.add(new UserInfo(rs.getString("first_name"),rs.getString("credential"),rs.getString("email_address")));
				}
			}
			logger.info("there are now "+list.size()+" users");
		} catch(SQLException sqle) {
			logger.error("oh no...."+sqle);
			if(AuthServlet.debugging) {
				list.add(new UserInfo("chris",null,null));
				list.add(new UserInfo("mike",null,null));
				list.add(new UserInfo("amy",null,null));
			}
			
		} finally {
			if(conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}
		return list;
	}
	
	public static void main(String[] args) {
		ConfluenceDBBasedUserInfoGetter x = new ConfluenceDBBasedUserInfoGetter();
		UserInfo ui = x.getUserInfoFor("chris");
		System.out.println("" + ui);
	}

}
