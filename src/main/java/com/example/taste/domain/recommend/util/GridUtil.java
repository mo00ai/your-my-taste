package com.example.taste.domain.recommend.util;

public class GridUtil {

	public static final int TO_GRID = 0;
	public static final int TO_GPS = 1;

	public static LatXLngY convert(int mode, double lat_X, double lng_Y) {
		double RE = 6371.00877;
		double GRID = 5.0;
		double SLAT1 = 30.0;
		double SLAT2 = 60.0;
		double OLON = 126.0;
		double OLAT = 38.0;
		double XO = 43;
		double YO = 136;

		double DEGRAD = Math.PI / 180.0;
		double RADDEG = 180.0 / Math.PI;

		double re = RE / GRID;
		double slat1 = SLAT1 * DEGRAD;
		double slat2 = SLAT2 * DEGRAD;
		double olon = OLON * DEGRAD;
		double olat = OLAT * DEGRAD;

		double sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
		sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);
		double sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
		sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;
		double ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
		ro = re * sf / Math.pow(ro, sn);

		LatXLngY rs = new LatXLngY();

		if (mode == TO_GRID) {
			rs.lat = lat_X;
			rs.lng = lng_Y;
			double ra = Math.tan(Math.PI * 0.25 + lat_X * DEGRAD * 0.5);
			ra = re * sf / Math.pow(ra, sn);
			double theta = lng_Y * DEGRAD - olon;
			if (theta > Math.PI)
				theta -= 2.0 * Math.PI;
			if (theta < -Math.PI)
				theta += 2.0 * Math.PI;
			theta *= sn;
			rs.x = Math.floor(ra * Math.sin(theta) + XO + 0.5);
			rs.y = Math.floor(ro - ra * Math.cos(theta) + YO + 0.5);
		} else {
			rs.x = lat_X;
			rs.y = lng_Y;
			double xn = lat_X - XO;
			double yn = ro - lng_Y + YO;
			double ra = Math.sqrt(xn * xn + yn * yn);
			if (sn < 0.0)
				ra = -ra;
			double alat = Math.pow((re * sf / ra), (1.0 / sn));
			alat = 2.0 * Math.atan(alat) - Math.PI * 0.5;

			double theta;
			if (Math.abs(xn) <= 0.0) {
				theta = 0.0;
			} else if (Math.abs(yn) <= 0.0) {
				theta = Math.PI * 0.5;
				if (xn < 0.0)
					theta = -theta;
			} else {
				theta = Math.atan2(xn, yn);
			}

			double alon = theta / sn + olon;
			rs.lat = alat * RADDEG;
			rs.lng = alon * RADDEG;
		}

		return rs;
	}

	public static class LatXLngY {
		public double lat;
		public double lng;
		public double x;
		public double y;
	}
}
