package omar.modules923.multidownload.fragments;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.fondesa.recyclerviewdivider.RecyclerViewDivider;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import omar.modules923.multidownload.Download.DownloadService;
import omar.modules923.multidownload.R;
 import omar.modules923.multidownload.adapters.databinding.RecyclerAdapter;
import omar.modules923.multidownload.adapters.databinding.RecyclerCallback;
import omar.modules923.multidownload.databinding.FrgDownloadsBinding;
import omar.modules923.multidownload.databinding.RowDownloadBinding;
import omar.modules923.multidownload.models.Download;

import static omar.modules923.multidownload.models.Download.STATUS_CANCELLED;

 
public class AllDownloads extends BaseFragment implements RecyclerAdapter.OnViewHolderClick

{

    private RecyclerAdapter downloadsAdapter;
    FrgDownloadsBinding binding ;

    public NotificationManagerCompat mNotificationManager;




    public AllDownloads() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        binding = DataBindingUtil.inflate(
                inflater, R.layout.frg_downloads, container, false);
        View rootView = binding.getRoot();


        return  rootView ;
    }

    @Override
    public void onResume()
    {

        super.onResume();
//        if (!getUserVisibleHint())
//        {
//            return;
//        }
        loadData();

    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initVars();

        mNotificationManager = NotificationManagerCompat.from(
                getActivity().getApplicationContext());

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext(),
                LinearLayoutManager.VERTICAL, false);


        RecyclerViewDivider.with(getActivity())
                .color(Color.GRAY)
                // OR
                .size(1)
                .build()
                .addTo( binding.rv );

        binding.rv.setLayoutManager(layoutManager);

        downloadsAdapter = new RecyclerAdapter(getActivity() ,binding.rv, new ArrayList<Object>(),
                R.layout.row_download, new RecyclerCallback() {
            @Override
            public void bindData(View viewHolderView, ViewDataBinding binder, Object model, int position) {

                setRecyclerData((RowDownloadBinding) binder, model, position);
            }


        }, this);

        binding.rv.setEmptyView(binding.txtvEmpty);
        binding.rv.setAdapter(downloadsAdapter);


      //  loadData();


    }

    @Override
    public void loadData() {

        downloadsAdapter.clearData();
        List<Download> downloadList =new ArrayList<>();

        downloadList =  new Select()
                .from(Download.class)
                .where("status   !=  ?   " , STATUS_CANCELLED )
                .orderBy("id ASC")
                .execute();


        for (int i = 0; i <downloadList.size() ; i++)

        {

            downloadsAdapter.addItem(i,downloadList.get(i));
        }


        binding.rv.setAdapter(downloadsAdapter);

    }

    @Override
    public void updateData(Download download) {


        boolean found=false;
        for (int i = 0; i <downloadsAdapter.getItemCount() ; i++) {

            if(download.getId().intValue()==((Download)downloadsAdapter.getItems().get(i)).getId().intValue())
            {
                  found=true;

                if(download.getStatus()==STATUS_CANCELLED)
                {

                    downloadsAdapter.getItems().remove(i);
                }
                else
                {
                    downloadsAdapter.getItems().set(i,download);

                }
                break;
            }

        }
        if(!found)
        {
            if(downloadsAdapter.getItemCount()>0)
            {
                downloadsAdapter.addItem(downloadsAdapter.getItemCount()-1,download);

            }
            else
            {
                downloadsAdapter.addItem(0,download);

            }

        }


        downloadsAdapter.notifyDataSetChanged();

    }

    private void setRecyclerData(final RowDownloadBinding binder, Object model, int position) {

        final Download download = (Download) model;
        binder.setItem(download);
        if(download.progress > 0)
        {
            binder.progressBar.setIndeterminate(false);
            binder.progressBar.setProgress((int)download.progress);
        }

        switch (download.getStatus()) {
            case Download.STATUS_CONNECTING:

                binder.imgvPauseResume.setVisibility(View.GONE);
                binder.imgvCancel.setVisibility(View.GONE);

                break;

            case Download.STATUS_DOWNLOADING:

                binder.imgvPauseResume.setVisibility(View.VISIBLE);
                binder.imgvCancel.setVisibility(View.VISIBLE);

                binder.imgvPauseResume.setBackgroundResource(R.drawable.ic_pause_circle_outline_black_36dp);

                break;
            case Download.STATUS_COMPLETE:
                binder.imgvPauseResume.setVisibility(View.GONE);
                binder.imgvCancel.setVisibility(View.VISIBLE);
                break;



            case Download.STATUS_PAUSED:
                binder.imgvPauseResume.setVisibility(View.VISIBLE);
                binder.imgvCancel.setVisibility(View.VISIBLE);

                binder.imgvPauseResume.setBackgroundResource(R.drawable.ic_play_circle_outline_black_36dp);

                break;
            case Download.STATUS_NOT_DOWNLOAD:
                binder.imgvPauseResume.setVisibility(View.VISIBLE);
                binder.imgvCancel.setVisibility(View.VISIBLE);

                binder.imgvPauseResume.setBackgroundResource(R.drawable.ic_play_circle_outline_black_36dp);

                break;
            case Download.STATUS_DOWNLOAD_ERROR:
                binder.imgvPauseResume.setVisibility(View.VISIBLE);
                binder.imgvCancel.setVisibility(View.VISIBLE);

                binder.imgvPauseResume.setBackgroundResource(R.drawable.ic_play_circle_outline_black_36dp);

                break;

            default:
                binder.imgvPauseResume.setVisibility(View.VISIBLE);
                binder.imgvCancel.setVisibility(View.VISIBLE);
                binder.imgvPauseResume.setBackgroundResource(R.drawable.ic_play_circle_outline_black_36dp);

                break;
        }

        binder.progressBar.setProgress((int)download.getProgress());
        binder.txtvProgress.setText(download.getStatusText() + ": " + download.progress + "%");


        binder.imgvPauseResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                switch (download.getStatus()) {

                    case Download.STATUS_DOWNLOADING:

                        binder.imgvPauseResume.setBackgroundResource(R.drawable.ic_play_circle_outline_black_36dp);
                        pauseDownload(download);


                        break;
                    case Download.STATUS_PAUSED:
                        binder.imgvPauseResume.setBackgroundResource(R.drawable.ic_pause_circle_outline_black_36dp);
                        resumeDownload(download);

                        break;


                    case Download.STATUS_NOT_DOWNLOAD:

                        binder.imgvPauseResume.setBackgroundResource(R.drawable.ic_play_circle_outline_black_36dp);
                        startDownload(download);


                        break;
                    case Download.STATUS_DOWNLOAD_ERROR:

                        binder.imgvPauseResume.setBackgroundResource(R.drawable.ic_play_circle_outline_black_36dp);
                        startDownload(download);


                        break;

                    default:
                        binder.imgvPauseResume.setBackgroundResource(R.drawable.ic_play_circle_outline_black_36dp);
                        startDownload(download);


                        break;
                }



            }
        });

        binder.imgvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                switch (download.getStatus()) {


                    case Download.STATUS_DOWNLOADING:

                        removeDownload(download);
                        cancelDownload(download);


                        break;


                    case Download.STATUS_PAUSED:
                        removeDownload(download);
                        cancelDownload(download);

                        break;

                    case Download.STATUS_COMPLETE:

                        removeDownload(download);
                        cancelDownload(download);

                        break;



                    case Download.STATUS_NOT_DOWNLOAD:

                        removeDownload(download);
                        deleteDownload(download);

                        break;
                    case Download.STATUS_DOWNLOAD_ERROR:

                        removeDownload(download);
                        deleteDownload(download);

                        break;

                    default:
                        removeDownload(download);
                        deleteDownload(download);

                        break;
                }


            }
        });
    }

    private void removeDownload(Download download) {

        downloadsAdapter.removeItemSqlite(download);
    }


    public void pauseDownload(Download download)
    {
        DownloadService.intentPause(getActivity(), download.getUrl());
    }

    public void resumeDownload(Download download)
    {
        DownloadService.intentResume(getActivity(), download.getUrl());
    }

    public void startDownload(Download download)
    {
        DownloadService.intentDownload(getActivity(), download.getUrl(), download);
    }

    public void cancelDownload(Download download)
    {

        deleteDownload(download);


        if(download.progress < 100)
        {
            DownloadService.intentCancel(getActivity(), download.getUrl());

        }

    }

    private void deleteDownload(Download download) {

        download.delete();

        mNotificationManager.cancel(
                download.getId().intValue() + 1000
        );

    }




    @Override
    public void onClick(RecyclerView recyclerView, View view, int position, Object model) {

    }







}
