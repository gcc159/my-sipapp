/**
 * Created by 郭骋城 on 2017/4/11.
 */
$(function () {
    $("#summit").click(function () {
        $.ajax({
            url:'http://127.0.0.1:8080/test/servlets/helloworld.html',
            type:'POST',
            async:true,
            data:{
                param1:'has Call'
            },
            timeout:4000,
            dataType:'json',
            beforeSend:function (xhr) {
                console.log(xhr)
                console.log('发送前')
            },
            success:function (data,textStatus) {
                console.log("正确");
                $("#txtTPStatus").value=data;
                console.log(data);
                console.log(data['param1'])

                console.log(textStatus)
            },
            error:function (xhr) {
                console.log("出错");
                console.log(xhr);
            }
        })

    })
})