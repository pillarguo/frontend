package user.normal.dao;

import java.io.IOException;
import javax.servlet.ServletException;
import user.normal.bean.MessageBean;
import user.normal.jwt.KeyUtil;
import user.normal.jwt.TokenUtil;



public class UsersAOP {

	public MessageBean put_auth (String user_id,String tokenstr)
			throws ServletException, IOException {
		MessageBean messageBean = new MessageBean();
		String id = "";// 从token中获取用户名
		String key = KeyUtil.getKey();// 获取秘钥		
		if (TokenUtil.isValid(tokenstr, key)) {// 检查token是否有效
			id = TokenUtil.getId(tokenstr, key);// 获取用户名					
			if(id.equals(user_id)){
				messageBean.setResultCode("1");
				messageBean.setResultMsg("可编辑");				
				return messageBean;
			}			
		}
		
		messageBean.setResultCode("0");
		messageBean.setResultMsg("不可编辑");
		return messageBean;
		
		
	}
}
