# RulerView
仿今日头条修改字体的控件
![image](/RulerView/Screenshot_20190405-155105.jpg)
How to

To get a Git project into your build:

Step 1. Add the JitPack repository to your build file

    gradle
    maven
    sbt
    leiningen

Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

Step 2. Add the dependency

	dependencies {
	        implementation 'com.github.ZhengJeff:RulerView:0.0.1'
	}

Share this release:
Tweet

Link

That's it! The first time you request a project JitPack checks out the code, builds it and serves the build artifacts (jar, aar).

If the project doesn't have any GitHub Releases you can use the short commit hash or 'master-SNAPSHOT' as the version.

See also

    Documentation

    Private Repositories

    Frequently Asked Questions

