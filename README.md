# x-msg-im-xsc-examples-java
java examples for libx-msg-im-xsc


* 这里提供了一些例子程序, 用于演示[libx-msg-im-xsc](https://github.com/dev5cn/libx-msg-im-xsc)的用法.


* 编译前准备

    * jdk 11.

    * maven 3.6.x or later.

    * git clone https://github.com/dev5cn/libmisc-java

    * git clone https://github.com/dev5cn/libxsc-proto-java

    * git clone https://github.com/dev5cn/x-msg-im-xsc-examples-java

    * 全部`git clone`后, 得到一个这样的目录结构:

    ```js
    libmisc-java  libxsc-proto-java x-msg-im-xsc-examples-java
    ```

* 编译

    * 依次进入`libmisc-java`, `libxsc-proto-java`, `x-msg-im-xsc-examples-java`, 在每个目录下执行`mvn package`.

    * 最后将得到一个可执行的jar: `x-msg-im-xsc-examples-java/target/x-msg-im-xsc-examples-java-0.0.1-SNAPSHOT.one-jar.jar`,  在执行它之间, 先启动[x-msg-im-xsc-examples-cpp](https://github.com/dev5cn/x-msg-im-xsc-examples-cpp).
    