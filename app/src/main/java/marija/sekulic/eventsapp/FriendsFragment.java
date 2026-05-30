package marija.sekulic.eventsapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class FriendsFragment extends Fragment {

    private AttendanceRepository attendanceRepository;
    private ArrayAdapter<String> listAdapter;
    private List<String> items = new ArrayList<>();

    public FriendsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        ListView listView = view.findViewById(R.id.lv_friends_activity);
        TextView txtEmpty = view.findViewById(R.id.txt_friends_empty);

        attendanceRepository = new AttendanceRepository(getContext());

        listAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1, items);
        listView.setAdapter(listAdapter);

        SharedPreferences sp = requireActivity().getSharedPreferences("UserData", requireActivity().MODE_PRIVATE);
        String serverUserId = sp.getString("current_server_id", "");

        if (serverUserId.isEmpty()) {
            txtEmpty.setVisibility(View.VISIBLE);
            txtEmpty.setText("Friends su dostupni samo kada ste online.");
            listView.setVisibility(View.GONE);
            return view;
        }

        attendanceRepository.getFriendsActivity(serverUserId, new RepositoryCallback<List<String>>() {
            @Override
            public void onResult(List<String> result) {
                items.clear();
                items.addAll(result);
                listAdapter.notifyDataSetChanged();
                if (result.isEmpty()) {
                    txtEmpty.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.GONE);
                } else {
                    txtEmpty.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String errorMessage) {
                txtEmpty.setVisibility(View.VISIBLE);
                txtEmpty.setText(errorMessage);
                listView.setVisibility(View.GONE);
            }
        });

        return view;
    }
}