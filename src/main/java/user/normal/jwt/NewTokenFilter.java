package user.normal.jwt;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import javax.inject.Singleton;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.ext.Provider;


@NewToken
@Provider
@Singleton 
public class NewTokenFilter implements  ContainerResponseFilter{
	
	@Override
	public void filter(ContainerRequestContext req, ContainerResponseContext resp) throws IOException {
		
		Map<String, Cookie> cookies = req.getCookies();
		String tokenstr = "";// 存放jwt字符串
		String nickname = "";// 存放昵称字符串
		String userId = "";// 存放用户Id字符串
		String roleType = "";// 存放用户角色字符串
		if (cookies != null) {
			for (String cookiekey : cookies.keySet()) {
				Cookie cookie = cookies.get(cookiekey);
				String cookieName = cookie.getName();
				if (cookieName.equals("jwtf")) {	
					tokenstr = cookie.getValue();
				}
				else if(cookieName.equals("nickname")){
					nickname=cookie.getValue();
				}
				else if(cookieName.equals("userId")){
					userId=cookie.getValue();
				}
				else if(cookieName.equals("roleType")){
					roleType=cookie.getValue();
				}
			}
		}

		String id = "";// 从token中获取用户id
		String role="";
		String key = KeyUtil.getKey();// 获取秘钥
		if (TokenUtil.isValid(tokenstr, key)) {// 检查token是否有效
			id = TokenUtil.getId(tokenstr, key);// 获取用户名
			role=TokenUtil.getSubject(tokenstr, key);
			// 生成token
			ExpirationTime exp = new ExpirationTime();
			Date expiration = exp.getExpiration();
			String jwtString = TokenUtil.getJWTString(id,role, expiration, KeyUtil.getKey());
			//String domain=DomainUtil.getDomain();
			//String domain = req.getHeaders("host");
			String domain =req.getHeaderString("host");
			
			//NewCookie(String name, String value, String path, String domain, String comment, int maxAge, boolean secure, boolean httpOnly)
			//NewCookie(Cookie cookie, String comment, int maxAge, Date expiry, boolean secure, boolean httpOnly)
			resp.getHeaders().add("Set-Cookie", new NewCookie("jwtf",jwtString,"/", domain, "tokenstr", 3600, false, true));
			resp.getHeaders().add("Cookiesss", new NewCookie("jwtf",jwtString,"/", domain, "tokenstr", 3600, false, true));
			resp.getHeaders().add("Set-Cookie", new NewCookie("nickname",nickname,"/", domain, "realName", 3600, false, false));
			resp.getHeaders().add("Set-Cookie", new NewCookie("userId",id,"/", domain, "cmdcAdminId", 3600, false, false));
			resp.getHeaders().add("Set-Cookie", new NewCookie("roleType",role,"/", domain, "roleType", 3600, false, false));
			
			return;
		}
		return;

	}

}
