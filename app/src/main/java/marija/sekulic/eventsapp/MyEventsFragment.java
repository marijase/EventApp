package marija.sekulic.eventsapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;



public class MyEventsFragment extends Fragment {
    Button btn_interested, btn_attending, btn_myprofile;

    public MyEventsFragment() {}  // Required empty public constructor


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* Fragment NEMA setContentView() . Umesto toga ima:  View view = inflater.inflate(...)
        *  setContentView je sluzilo u svim aktiviti klasama da poveze UI i nakon ovoga
        *                 je islo findViewById
        *  U fragmentima: View view = inflater.inflate(...)
        *                      view.findViewById(...)
        */
    }

    // zasto logika u onCreateView a ne onCreate -> Fragment nema svoj UI dok ga ne napraviš
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {

        // ovde se ucitava xml (Inflate the layout for this fragment) :
        View view = inflater.inflate(R.layout.fragment_my_events, container, false);

        btn_interested = view.findViewById(R.id.id_btn_interested);
        btn_attending = view.findViewById(R.id.id_btn_attending);
        btn_myprofile = view.findViewById(R.id.id_btn_myprofile);

        // klik interested
        btn_interested.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // getActivity ili requireContext svejedno
                Intent open_interested = new Intent(getActivity(), InterestedEventsActivity.class );
                /* zasto ne moze 'this' kao u aktivitiju
                Fragment živi UNUTAR Activity-ja, Zato: ne može sam da pokreće stvari
                mora preko Activity-ja (konteksta)
                */
                startActivity(open_interested);
            }
        });
        // klik attending events
        btn_attending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent open_attending = new Intent(getActivity(), AttendingEventsActivity.class);
                startActivity(open_attending);
            }
        });
        //klik my profile
        btn_myprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent open_myprofile = new Intent(getActivity(), ProfileActivity.class);
                startActivity(open_myprofile);
            }
        });

        return view;
        //return inflater.inflate(R.layout.fragment_my_events, container, false);
    }
}