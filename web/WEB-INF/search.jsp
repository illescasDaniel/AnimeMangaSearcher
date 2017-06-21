<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>Search</title>
		<link rel="stylesheet" href="../commonStyles.css">
		<link rel="apple-touch-icon-precomposed" href="apple-touch-icon.png">
		<meta name="HandheldFriendly" content="True">
		<meta name="MobileOptimized" content="320">
		<meta name="viewport" content="width=device-width, initial-scale=1">
	</head>
	<body>
		<section class="itemsAtLeft">
            <a href="../index.jsp?logout">Log out</a>
        </section>
		<section class="fullWidth centeredBody">
			<h1>Search:</h1>
			<form class="centeredBody fullWidth" action="search" method="get">
				<input class="searchInputStyle" name="title" type="text" placeholder="Anime/Manga title">
			</form>
		</section>
		<div class="searchStyle">${mangas}</div>
	</body>
</html>