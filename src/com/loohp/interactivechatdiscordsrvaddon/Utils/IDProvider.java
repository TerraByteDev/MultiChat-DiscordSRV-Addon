package com.loohp.interactivechatdiscordsrvaddon.Utils;

import java.util.concurrent.atomic.AtomicInteger;

public class IDProvider {
	
	private AtomicInteger counter;
	
	public IDProvider() {
		this.counter = new AtomicInteger(0);
	}
	
	public int getNext() {
		int value = counter.get();
		counter.set(value == Integer.MAX_VALUE ? 0 : value + 1);
		return value;
	}

}
