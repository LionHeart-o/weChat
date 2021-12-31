# 基于android的通讯app

此文档为该通讯app的简述，只笼统的讲述该程序的实现逻辑，不周之处望见谅。

## 一.UI介绍

### 1.登录界面

![](https://cdn.jsdelivr.net/gh/LionHeart-o/hexo/blogimg/1615805615(1).jpg)

### 2.注册页面

![](https://cdn.jsdelivr.net/gh/LionHeart-o/hexo/blogimg/image-20210315185454177.png)

### 3.忘记密码发送验证码页面

![](https://cdn.jsdelivr.net/gh/LionHeart-o/hexo/blogimg/image-20210315185629421.png)

### 4.修改密码页面

![](https://cdn.jsdelivr.net/gh/LionHeart-o/hexo/blogimg/image-20210315185647539.png)

### 5.主页面（联系人页面）

![](https://cdn.jsdelivr.net/gh/LionHeart-o/hexo/blogimg/image-20210723024757635.png)

### 6.添加好友页面

![](https://cdn.jsdelivr.net/gh/LionHeart-o/hexo/blogimg/1615806021(1).jpg)





![](https://cdn.jsdelivr.net/gh/LionHeart-o/hexo/blogimg/ee237423858bd5384b6196da0bd2412.jpg)

### 7.个人信息页面

![](https://cdn.jsdelivr.net/gh/LionHeart-o/hexo/blogimg/1615805960(1).jpg)

### 8.聊天页面

![](https://cdn.jsdelivr.net/gh/LionHeart-o/hexo/blogimg/1615806090(1).jpg)

![](https://cdn.jsdelivr.net/gh/LionHeart-o/hexo/blogimg/image-20210723025028431.png)

![](https://cdn.jsdelivr.net/gh/LionHeart-o/hexo/blogimg/image-20210723025055998.png)



### 9.朋友圈页面

朋友圈主页：

![](https://cdn.jsdelivr.net/gh/LionHeart-o/hexo/blogimg/1627898032(1).jpg)

朋友圈发送：

![](https://cdn.jsdelivr.net/gh/LionHeart-o/hexo/blogimg/image-20210802180147646.png)





朋友圈评论与回复：

![](https://cdn.jsdelivr.net/gh/LionHeart-o/hexo/blogimg/image-20210802180353923.png)



## 二.功能介绍

### 1.实现了注册功能

用户需要填写邮箱、用户名、密码、验证码。

密码会根据正则表达式判断合法性，并且会经过算法加密之后再上传到数据库。

![](https://cdn.jsdelivr.net/gh/LionHeart-o/hexo/blogimg/1615806612(1).jpg)

![](https://cdn.jsdelivr.net/gh/LionHeart-o/hexo/blogimg/1615806448(1).jpg)

点击获取验证码后，会生成六位数验证码并发送到用户填写的邮箱中。

![](https://cdn.jsdelivr.net/gh/LionHeart-o/hexo/blogimg/1615806359(1).jpg)

为了方便调试，留了一个BUG。填写一个正确的邮箱获取验证码后，改变自己填写的邮箱也可以注册成功。



### 2.实现了获取验证码，并修改密码的功能

逻辑和注册功能差不多，不多做介绍。

### 3.实现了登录和自动登录功能

输入正确的账号密码进行登录之后，程序会在手机数据库存储下这个账号密码，下次打开程序的时候会自动检索这个账号密码并向spring后端发送登录请求。

```java
private boolean autoLogin(){
    helper = new SQLAutoLogin(this);
    SQLiteDatabase database = helper.getReadableDatabase();
    helper.onCreate(database);
    Cursor cursor = helper.query();
    if(cursor.getCount()==0){
        return false;
    }else {
        cursor.moveToFirst();
      requireLogin(cursor.getString(cursor.getColumnIndex("email")),cursor.getString(cursor.getColumnIndex("password")));
        return true;
    }
}
```

在个人信息页面点击退出登录后，程序会删除掉在本地数据库保存的账号密码。



### 4.实现了好友添加功能

在聊天页面右上角点击“+”号，即会弹出窗口添加好友。

![](https://cdn.jsdelivr.net/gh/LionHeart-o/hexo/blogimg/1615806744(1).jpg)

发送好友请求后，对方会在系统消息上看到好友请求。

![](https://cdn.jsdelivr.net/gh/LionHeart-o/hexo/blogimg/1615806904(1).jpg)

对方点击拒绝后，可以看到系统消息上显示的好友申请拒绝的消息。

![](https://cdn.jsdelivr.net/gh/LionHeart-o/hexo/blogimg/1615806925(1).jpg)

点击同意后聊天页面即会显示好友。



### 5.实现了聊天功能

可以发送**文本信息**和**图片信息**，所有聊天信息皆存储在本地手机数据库,文件信息会存储到手机，数据库中存储该文件的地址。

文本信息发送无需赘述，聊天功能的实现使用了**websocket**，收到信息后会弹出通知。

![](https://cdn.jsdelivr.net/gh/LionHeart-o/hexo/blogimg/image-20210723030727430.png)

图片信息发送的逻辑为：

本地选择图片，转为base64编码，通过websocket上传到服务器->

```java
    private void sendPicture(){
        try {
            PictureSelector.create(ChatActivity.this)
                    .openGallery(PictureMimeType.ofImage())
                    .imageEngine(GlideEngine.createGlideEngine())
                    .isWeChatStyle(true)
                    .forResult(new OnResultCallbackListener<LocalMedia>() {
                        @Override
                        public void onResult(List<LocalMedia> result) {
                            // 结果回调,这里写发送图片给好友的方法
                            //1.获取图片
                            for(int i=0;i<result.size();i++){
                                String realPath=result.get(i).getRealPath();
                                File file = new File(realPath);
                                //2.将图片转为字节数据，发送给好友
                                try {
                                    ChatBean chatBean = new ChatBean();
                                    chatBean.setMessage(result.get(0).getRealPath());
                                    chatBean.setHeadDetail(ownHead);
                                    chatBean.setState(chatBean.SEND);//SEND表示自己发送的信息
                                    chatBean.setMessageType(chatBean.PIC);
                                    chatBeanList.add(chatBean);//将发送的信息添加到chatBeanList集合中
                                    adpter.notifyDataSetChanged();//更新ListView列表
                                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    //将该信息插入到本地数据库
                                    helper.insert(ownEmail,contactEmail,realPath,4,df.format(new Date()));
                                    //2代表的是发送的是非文本消息
                             				 wsManager.sendInfo(ownEmail,contactEmail,encodeBase64File(realPath),2,file.getName());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            //3.适配器更新视图
                            adpter.notifyDataSetChanged();
                        }
                        @Override
                        public void onCancel() {
                            // 取消
                        }
                    });

        } finally {

        }
    }

```

服务器将此信息转发给另外一个用户->

联系人将base64编码转回为图片并存储到本地->

```java
        public void toFile(String base64Code,String targetPath) throws Exception {
            byte[] buffer = Base64.decode(base64Code,1);
            FileOutputStream out = new FileOutputStream(targetPath);
            out.write(buffer);
            out.close();
        }
```

并将图片路径存储到数据库中

### 6.实现了文件传输功能

可以向联系人传输文件。

文件传输的流程为：

调用手机自带的文件管理器，让用户选择文件。

![](https://cdn.jsdelivr.net/gh/LionHeart-o/hexo/blogimg/image-20210723025410971.png)

客户端A选择文件之后，文件通过ftp服务端上传到服务器（下图为filezilla查看到的服务器中的上传文件）

![](https://cdn.jsdelivr.net/gh/LionHeart-o/hexo/blogimg/image-20210723025710107.png)

上传成功后，客户端A将收到上传成功的返回消息，然后客户端A将文件上传的地址发送给客户端B，将信息类型标识为”文件类型“，客户端B通过websocket接收到客户端A的信息，读取到信息是文件类型，会下载信息附带的url地址中的文件，存储到本地中，然后将这条信息插入到手机数据库，文件地址为本机文件地址，再以文件信息格式展现，程序会根据传输的文件的类型切换不同的ui样式。

![](https://cdn.jsdelivr.net/gh/LionHeart-o/hexo/blogimg/image-20210723025956291.png)

点击文件，程序会自动获取手机中可以打开此文件的应用，选择相应的应用即可打开。

<img src="https://cdn.jsdelivr.net/gh/LionHeart-o/hexo/blogimg/image-20210723030259277.png" style="zoom: 33%;" />

### 7.实现了个人信息编辑功能

可以更改用户名。

实现了更改头像的功能，本功能依赖了PictureSelector。点击个人信息页的头像会进入到图片上传页面。

![](https://cdn.jsdelivr.net/gh/LionHeart-o/hexo/blogimg/1615807493(1).jpg)



选择图片后进入裁剪，点击完成后，即可更换头像。

![](https://cdn.jsdelivr.net/gh/LionHeart-o/hexo/blogimg/1615807729(1).jpg)

头像更换的逻辑为：

本地选择图片上传->nodejs服务器接收到图片->nodejs将图片存储到服务器的相对路径中，以用户的邮箱作为图片唯一名称->刷新客户端，清除头像缓存，可看到用户头像变更。

### 8.实现了朋友圈功能

可以自由选择图片和自由输入文本，选择图片这块依赖了PictureSelector。

github地址为：https://github.com/LuckSiege/PictureSelector

![](https://cdn.jsdelivr.net/gh/LionHeart-o/hexo/blogimg/image-20210802180316770.png)

能拖动图片删除

![](https://cdn.jsdelivr.net/gh/LionHeart-o/hexo/blogimg/1627918527(1).png)

说说能以九宫格形式展示图片，能评论和回复评论

![](https://cdn.jsdelivr.net/gh/LionHeart-o/hexo/blogimg/1627919350(1).jpg)



