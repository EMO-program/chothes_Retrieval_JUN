<!DOCTYPE HTML>
<html>
<head>
<title>Home</title>
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<script type="application/x-javascript"> addEventListener("load", function() { setTimeout(hideURLbar, 0); }, false); function hideURLbar(){ window.scrollTo(0,1); } </script>
<link href="css/bootstrap.css" rel='stylesheet' type='text/css' />
<!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
<!-- Custom Theme files -->
<link href="css/style.css" rel='stylesheet' type='text/css' />

<!-- Custom Theme files -->
<!--webfont-->
<script type="text/javascript" src="js/jquery-1.11.1.min.js"></script>
<script src="text/javascript" src="js/menu_jquery.js"></script>
</head>
<body>
	<div class="header">
      <div class="container">
  	     <div class="logo">
			<h1><a href="index.html">找布易</a></h1>
		 </div>
		 <div class="top_right">
		   <ul>
			<li><a href="register.html">注册</a></li>|
			<li class="login" >
				 <div id="loginContainer"><a href="#" id="loginButton"><span>登录</span></a>
					  <div id="loginBox">
						  <form id="loginForm">
			                <fieldset id="body">
			                	<fieldset>
			                          <label for="email">Email Address</label>
			                          <input type="text" name="email" id="email">
			                    </fieldset>
			                    <fieldset>
			                            <label for="password">Password</label>
			                            <input type="password" name="password" id="password">
			                     </fieldset>
			                    <input type="submit" id="login" value="Sign in">
			                	<label for="checkbox"><input type="checkbox" id="checkbox"> <i>Remember me</i></label>
			            	</fieldset>
			                 <span><a href="#">Forgot your password?</a></span>
						   </form>
				        </div>
			      </div>
			  </li>
		   </ul>
	     </div>
		 <div class="clearfix"></div>
		</div>
	</div>

  <#include "index.ftl">

  <div class="grid_3">
	  <div class="container">
	  	 <ul id="footer-links">
			<li><a href="support.html">Support</a></li>
			<li><a href="about.html">About Us</a></li>
			<li><a href="faq.html">FAQ</a></li>
			<li><a href="#">Categories</a></li>
         </ul>
         <!--<p>Copyright &copy; 2015.Company name All rights reserved.More Templates </p> -->
	  </div>
	</div>


</body>
</html>
