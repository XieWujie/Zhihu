package com.example.downloadhelp.request;

import android.widget.ImageView;
import com.example.downloadhelp.DL;
import com.example.downloadhelp.DLManager;
import com.example.downloadhelp.DLUtil.Executors;
import com.example.downloadhelp.cache.Fetch;
import com.example.downloadhelp.cache.Save;
import com.example.downloadhelp.converter.ReadConverter;
import com.example.downloadhelp.target.ImageViewTarget;
import com.example.downloadhelp.target.Target;
import com.example.downloadhelp.task.FileTask;
import com.example.downloadhelp.task.Task;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;

public class DLRequestBuilder<ResourceType> extends RequestOptions<DLRequestBuilder<ResourceType>,ResourceType> {

    private DL dl;
    private DLManager dlManager;
    private ReadConverter<ResourceType> readConverter;
    private Task task;
    private Save save;
    private Fetch fetch;
    private Target target;
    private RequestOptions options;


    public DLRequestBuilder<ResourceType> load(@NotNull String url){
        this.url = url;
        return this;
    }


    public DLRequestBuilder(DL dl, DLManager dlManager, ReadConverter<ResourceType> readConverter, Class<ResourceType> resourceType) {
        this.dl = dl;
        this.dlManager = dlManager;
        this.readConverter = readConverter;
        this.resourceType = resourceType;
        this.save = dl.getSave();
        this.fetch = dl.getFetch();
    }

    public Request<ResourceType> submit(){
       return buildObservableRequest();
    }

    public DLRequestBuilder apply(Task task){
        this.task = task;
        return this;
    }



    public DLRequestBuilder<ResourceType>apply(ReadConverter<ResourceType> readConverter){
        this.readConverter = readConverter;
        return this;
    }

    public DLRequestBuilder<ResourceType> apply(RequestOptions options){
        this.options = options;
        return this;
    }

    public Request<ResourceType> into(@NotNull Target target){
        this.target = target;
        return buildSimpleRequest();
    }

    public Request<ResourceType> into(@NotNull ImageView imageView){
        return into(new ImageViewTarget(imageView));
    }

    private Request<ResourceType> buildSimpleRequest(){
        if (task == null){
            task = new FileTask(getOptions(),dl.getExecutor());
        }
        SimpleRequest<ResourceType> request = SimpleRequest.obtain();
        Executor callbackExecutor = target==null?Executors.directExecutor():Executors.mainExecutor();
        request.init(this,task,save,fetch,dl.getExecutor(),callbackExecutor,readConverter,target);
        dlManager.addRequest(url,request);
        return request;
    }
    private Request<ResourceType> buildObservableRequest(){
        if (task == null){
            task = new FileTask(getOptions(),dl.getExecutor());
        }
        ObservableRequest<ResourceType> request = new ObservableRequest<>();
        Executor callbackExecutor = target==null?Executors.directExecutor():Executors.mainExecutor();
        request.init(this,task,save,fetch,dl.getExecutor(),callbackExecutor,readConverter,target);
        dlManager.addRequest(url,request);
        return request;
    }

    public RequestOptions getOptions(){
        if (options != null){
            return options;
        }
        if (fileName == null){
            int lastIndex  = url.lastIndexOf("?");
            fileName = url.substring(url.lastIndexOf("/"),lastIndex==-1?url.length():lastIndex);
        }
        if (parentPath == null){
            parentPath = dl.getDefaultPath();
        }
        return (RequestOptions)super.clone();
    }
}
