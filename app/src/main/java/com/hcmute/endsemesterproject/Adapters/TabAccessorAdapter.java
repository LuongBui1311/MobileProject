package com.hcmute.endsemesterproject.Adapters;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.hcmute.endsemesterproject.Controllers.BetaGroupFragment;
import com.hcmute.endsemesterproject.Controllers.ChatsFragment;
import com.hcmute.endsemesterproject.Controllers.ContactsFragment;
import com.hcmute.endsemesterproject.Controllers.GroupsFragment;
import com.hcmute.endsemesterproject.Controllers.RequestsFragment;

public class TabAccessorAdapter extends FragmentPagerAdapter
{

    public TabAccessorAdapter(FragmentManager fm)
    {
        super(fm);
    }

    @Override
    public Fragment getItem(int i)
    {
        switch (i)
        {
            case 0:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;
            case 2:
                ContactsFragment contactsFragment = new ContactsFragment();
                return contactsFragment;

            case 3:
                RequestsFragment requestsFragment = new RequestsFragment();
                return requestsFragment;
            case 1:
                BetaGroupFragment betaGroupFragment = new BetaGroupFragment();
                return betaGroupFragment;

            default:
                return null;
        }
    }


    @Override
    public int getCount()
    {
        return 4;
    }


    @Nullable
    @Override
    public CharSequence getPageTitle(int position)
    {
        switch (position)
        {
            case 0:
                return "Chats";

            case 2:
                return "Contacts";

            case 3:
                return "Requests";
            case 1:
                return "Beta groups";
            default:
                return null;
        }
    }
}