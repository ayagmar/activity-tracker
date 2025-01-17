package com.ayagmar.activitytracker.process;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;

public class WindowIterator implements Iterator<WinDef.HWND> {
    private WinDef.HWND current;

    WindowIterator(WinDef.HWND start) {
        this.current = start;
    }

    @Override
    public boolean hasNext() {
        return current != null;
    }

    @Override
    public WinDef.HWND next() {
        if (!hasNext()) throw new NoSuchElementException();
        WinDef.HWND result = current;
        current = User32.INSTANCE.GetWindow(current, new WinDef.DWORD(WinUser.GW_HWNDNEXT));
        return result;
    }

    public Spliterator<WinDef.HWND> spliterator() {
        return Spliterators.spliteratorUnknownSize(this, Spliterator.ORDERED);
    }
}
