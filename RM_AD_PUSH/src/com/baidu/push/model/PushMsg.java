package com.baidu.push.model;

public class PushMsg {
	int type;
	Msg data;

	public Msg getData() {
		return data;
	}

	public void setData(Msg data) {
		this.data = data;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public class Msg {
		HH msg;

		public HH getMsg() {
			return msg;
		}

		public void setMsg(HH msg) {
			this.msg = msg;
		}

	}

	public class HH {
		String content;
		String url;

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}
	}
}
