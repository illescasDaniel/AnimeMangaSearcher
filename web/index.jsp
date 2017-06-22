<%--
  Created by IntelliJ IDEA.
  User: Daniel
  Date: 17/06/2017
  Time: 12:44
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html class="HTMLheight centeredBody">
	<head>
		<meta charset="UTF-8">
		<title>Anime-Manga Searcher</title>
		<link rel="stylesheet" href="commonStyles.css">
		<link rel="apple-touch-icon-precomposed" href="WEB-INF/apple-touch-icon.png">
		<meta name="HandheldFriendly" content="True">
		<meta name="MobileOptimized" content="320">
		<meta name="viewport" content="width=device-width, initial-scale=1">
	</head>
	<body class="centeredBody">
	<h1 class="indexTitle">Welcome, log in:</h1>
	<%
		Boolean validCredentials = (session.getAttribute("username") != null && session.getAttribute("password") != null);

		if (request.getParameter("logout") != null) {
			if (validCredentials) {
				session.invalidate();
			}
		}
		else if (validCredentials) {
			request.getRequestDispatcher("/WEB-INF/search.jsp").forward(request, response);
		}
	%>
	<form class="centeredItems" action="search" method="post">
		<div class="centeredItems">
			<input class="inputsStyle" type="text" name="username" placeholder="Username">
			<input class="inputsStyle" type="password" name="password" placeholder="Password">
		</div>
		<input class="buttonStyle" id="submitButton" type="submit" value="Enter">
	</form>

	<p class="infoParagraphInLogin">Use your credentials from <a href="https://myanimelist.net" class="infoParagraphInLogin">MyAnimeList</a></p>
	<p class="errorMessageInLogin">${errorMessage}</p>
	</body>
</html>