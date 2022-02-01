package io.github.contractautomata.RunnableOrchestration;

import java.util.ArrayList;

public class AutoCloseableList<T  extends AutoCloseable > extends ArrayList<T> implements AutoCloseable {

	private static final long serialVersionUID = 1L;

	@Override
	public void close() {
		this.stream().forEach(el -> {
			if (el != null)
				try {
					el.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		});
		
	}

}
