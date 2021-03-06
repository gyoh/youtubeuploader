Requirements
------------

* [scala](http://www.scala-lang.org/)
* [sbt-0.7](https://github.com/mpeltonen/sbt-idea/tree/sbt-0.7)

Usage
-----

Obtain your YouTube developer key and place it as the developerKey value.

    val developerKey = "<developer_key>"

Load up SBT in the project's top directory:

    gyoh$ sbt

To upload all the mp4 or m4v files under the certain directory to YouTube and embed them in your tumblelog, run the program as follows:

    > run [directory path] [YouTube name] [YouTube password] [tumblr name] [tumblr email] [tumblr password]

tumblr name is the subdomain of your tumblr URL.
For instance, if your tumblr URL is "foo.tumblr.com", then your tumblr name is "foo".

If you do not use tumblr and just want to upload videos to YouTube, run the program as follows:

    > run [directory path] [YouTube name] [YouTube password]

Notes
-----

The code does not handle the quota issue mentioned below:

* http://gyo.hamamoto.org/post/49440033897/youtube-api-blog-best-practices-for-avoiding
