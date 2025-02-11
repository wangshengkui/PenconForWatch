package com.example.pencon;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.mgesture.MGesture;
import com.example.mgesture.MGestureStroke;
import com.example.mgesture.MGestureUnitils;
import com.example.pencon.WifiUtil.WifiCipherType;
import com.example.readAndSave.SmartPenPage;
import com.example.readAndSave.SmartPenUnitils;
import com.example.smartpengesture.DealSmartPenGesture;
import com.example.smartpengesture.Position;
import com.example.smartpengesture.SmartPenGesture;
import com.google.common.collect.ArrayListMultimap;
import com.tqltech.tqlpencomm.Dot;
import com.tqltech.tqlpencomm.Dot.DotType;
import com.tqltech.tqlpencomm.PenCommAgent;
import com.tqltech.tqlpencomm.util.BLEFileUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GesturePoint;
import android.gesture.GestureStroke;
import android.gesture.Prediction;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {

	private boolean bIsReply = false;
	private ImageView gImageView;
	private RelativeLayout gLayout;
	private DrawView[] bDrawl = new DrawView[2]; // add 2016-06-15 for draw
	private final static String TAG = "OidActivity";
	private final static boolean isSaveLog = false; // 是否保存绘制数据到日志
	private final static String LOGPATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/TQL/"; // 绘制数据保存目录
	private LinkedList<Dot> dotsQueen = new LinkedList<Dot>();
// private BluetoothLEService mService = null; //蓝牙服务

	private static final int REQUEST_SELECT_DEVICE = 1; // 蓝牙扫描
	private static final int REQUEST_ENABLE_BT = 2; // 开启蓝牙
//	private static final int REQUEST_LOCATION_CODE = 100; // 请求位置权限
	private static final int GET_FILEPATH_SUCCESS_CODE = 1000;// 获取txt文档路径成功

	private int penType = 1; // 笔类型（0：TQL-101 1：TQL-111 2：TQL-112 3: TQL-101A）

	private double XDIST_PERUNIT = Constants.XDIST_PERUNIT; // 码点宽
	private double YDIST_PERUNIT = Constants.YDIST_PERUNIT; // 码点高
	private double A5_WIDTH = Constants.A5_WIDTH; // 本子宽
	private double A5_HEIGHT = Constants.A5_HEIGHT; // 本子高
	public double A4_WIDTH = Constants.A4_WIDTH; // 本子宽
	public double A4_HEIGHT = Constants.A4_HEIGHT; // 本子高
	private double A5_BG_REAL_WIDTH = Constants.A5_BG_REAL_WIDTH; // 资源背景图宽
	private double A5_BG_REAL_HEIGHT = Constants.A5_BG_REAL_HEIGHT; // 资源背景图高
	private double A4_BG_REAL_WIDTH = Constants.A4_BG_REAL_WIDTH; // 资源背景图宽
	private double A4_BG_REAL_HEIGHT = Constants.A4_BG_REAL_HEIGHT; // 资源背景图高

	private int BG_WIDTH; // 显示背景图宽
	private int BG_HEIGHT; // 显示背景图高
	private int A5_X_OFFSET; // 笔迹X轴偏移量
	private int A5_Y_OFFSET; // 笔迹Y轴偏移量
//	private int gcontentLeft; // 内容显示区域left坐标
//	private int gcontentTop; // 内容显示区域top坐标

	public static float mWidth; // 屏幕宽
	public static float mHeight; // 屏幕高

	private float mov_x; // 声明起点坐标
	private float mov_y; // 声明起点坐标
	public int gCurPageID = -1; // 当前PageID
	public int gCurBookID = -1; // 当前BookID
	private float gScale = 1; // 笔迹缩放比例
	private int gColor = 6; // 笔迹颜色
	private int gWidth = 3; // 笔迹粗细
	private int gSpeed = 30; // 笔迹回放速度
	private float gOffsetX = 0; // 笔迹x偏移
	private float gOffsetY = 0; // 笔迹y偏移

	private ArrayListMultimap<Integer, Dots> dot_number = ArrayListMultimap.create(); // Book=100笔迹数据
	private ArrayListMultimap<Integer, Dots> dot_number1 = ArrayListMultimap.create(); // Book=0笔迹数据
	private ArrayListMultimap<Integer, Dots> dot_number2 = ArrayListMultimap.create(); // Book=1笔迹数据
	private ArrayListMultimap<Integer, Dots> dot_number4 = ArrayListMultimap.create(); // 笔迹回放数据
	private Intent serverIntent = null;
	private Intent LogIntent = null;
	private PenCommAgent bleManager;
	private String penAddress;
	public ArrayList<String> filenames = new ArrayList<String>();
	public static float g_x0, g_x1, g_x2, g_x3;
	public static float g_y0, g_y1, g_y2, g_y3;
	public static float g_p0, g_p1, g_p2, g_p3;
	public static float g_vx01, g_vy01, g_n_x0, g_n_y0;
	public static float g_vx21, g_vy21;
	public static float g_norm;
	public static float g_n_x2, g_n_y2;

	public Map<String, ArrayList<SimplePoint>> gesture = new HashMap<String, ArrayList<SimplePoint>>();// String是笔划序数为1，2，3，……的字符串
//  public ArrayList<SimplePoint> gestureStokeBufferArrayList=new ArrayList<SimplePoint>();
	private ArrayList<SimplePoint> points = new ArrayList<SimplePoint>();

	private int gPIndex = -1;
	private boolean gbSetNormal = false;
	private boolean gbCover = false;

	private float pointX;
	private float pointY;
	private int pointZ;

	private boolean bIsOfficeLine = false;
	// private RoundProgressBar bar;
	private BluetoothLEService mService = null; // 蓝牙服务

	// private RelativeLayout dialog;
	// private Button confirmBtn;
	private TextView showInftTextView;
	TextView penStatus;
	float firstPointX = 0;
	private float firstPointY = 0;
	private double firstPointTime = 0;
	private int haveRequest = 0;

	private float gpointX;
	private float gpointY;

	private String gStrHH = "";
	private boolean bLogStart = false;

	public int mN;
	public static String studentNumber = "0944";// 初始化不存在的学号
	public SmartPenPage smartPenPage = null;
	public int mgroupedNumber = -1;

	RelativeLayout informationLayout;
	LinearLayout pen_chirography_all;
	RelativeLayout pencage;
	TextView peninfo;
	/* ImageView groupinfo; */
	TextView gestureinfo;//
	String gestureInfoString = "";
	public String taillength = "";
	public String physicsTaillength="";
	public String gesturelength = "";
	public String physicsgesturelength="";
	public String boundingBoxWidth = "";
	public String boundingBoxheight = "";
	public String taileChangeTimes = "";
	public String tailPointCounter = "";
	public String tailSlope = "";
	public String centerX= "";
	public String centerY = "";	
	public  String  bodySlope="";
	public String  bodyAndBodyBoxRation="";
	public final int TAILLENGTH = 1;
	public final int GESTURELENGTH = 2;
	public final int BOUNDINGBOXWIDTH = 3;
	public final int BOUNDINGBOXHEIGHT = 4;
	public final int TAILECHANGETIMES = 5;
	public final int TAILPOINTCOUNTER = 6;
	public final int TAILSLOPE = 7;
	public final int PHYSICSTAILLENGTHPHYSICSTAILLENGTH = 8;
	public final int PHYSICSGESTURELENGTH = 9;
	public final int CENTERX=10;
	public final int CENTERY=11;
	public final int BODYSLOPE=12;
	public final int BODYANDBODYBOXRATION=13;
	/* 最小显示版面变量开始 */
	RelativeLayout minishowlayout;// 最小显示版面
	RelativeLayout selfWriteStatus;// 自我书写状态
	RelativeLayout verbal;// 语文
	RelativeLayout math;// 数学
	RelativeLayout english;// 英语
	RelativeLayout groupStatus;// 群组状态
	int witchShow = 0;// 最小显示版面中的控件哪一个在显示，自我书写状态为0，语文：1…………，群组状态：4
	ImageView leftarrow;
	ImageView rightarrow;
	/* 最小显示版面变量 结束 */
//0122	
	ArrayList<Integer> questionsContainer = new ArrayList<Integer>();
	// public boolean grouped=false;

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE_SECURE = 3000;
	private static final int REQUEST_CONNECT_DEVICE_INSECURE = 3001;
	// private static final int REQUEST_ENABLE_BT = 3;
	private BluetoothChatService mChatService = null;
	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	public volatile int groupedNumber = 0;
	public volatile int otherGroupedNumber = 0;// 表示其他平板发送过来的组队码
	public volatile int lastOidNumber = 0;// 判断组队模式时，上一个点读码的值
	public volatile long lastpointTime = 0;// 为判断组队之前是否时点读还是组对
	public volatile long lastOidTime = 0;// 最后来的点读码的时间
	public volatile long firstTime = 0;// 笔刚点下时来的点读码的时间
	public volatile boolean firstTOBeStatus = true;
	public static final int GROUP_REQUEST = 111;// 发送组对请求需要的前缀
	public static final int GROUP_ANSWER = 110;// 发送组对请求需要的前缀
	public static final int NORMAL_Message = 101;// 普通的字符串信息
	public static final int DISSOLVE_GROUP = 100;// 普通的字符串信息
	public static final int GROUP_ALREADY = 011;// 表示其他人同意请求后，点击在了组对码上;
	public static final int GROUP_NUMBER_WRONG = 010;// 表示和组对发起者（组长）的组对码不想等
	public static final int GROUP_SUCESS = 001;
	public static final int GROUP_CANCLE = 000;
	public static final int HOMEWORK = 1000;
	public int groupLeader = 1;// 等于零表示不是组长
	private byte[] mbyte;
	private StringMessage mStringMessage;
	public DealSmartPenGesture dealSmartPenGesture = new DealSmartPenGesture();
	public AlertDialog builder;
//    public RecordingService.MyBinder myRecordBinder;
//    public String myRecordFilePathString="";
	public String soundPathString = "";
	public MGestureUnitils mGestureUnitils = new MGestureUnitils("/sdcard/zgmgesture.zgm");
//	LinearLayout penChirography;

	public static final int PENUP = 0;
	public static final int PENDOWN = 1;
	public volatile int penUpOrDown = PENUP;
	public volatile int otherPenUpOrDown = PENUP;
	public volatile boolean otherPenHaveDown = false;
	public boolean sendedMessage = false;// 非组长人员已经给组长发送过信息了
	public String mNameString = "A";
	public String otherNameString = "theOther";
	/*
	 * 1. groupstatusstatus=0 未组对 2. groupstatus=1 发送完组队通知等待答复 3. groupstatus=2
	 * 发送完组对通知后得到接受组对答复 4. groupstatus=3 发送完组对通知后得到拒绝组对的答复 5. groupstatus=4
	 * 发送完组对成功等待答复 6. groupstatus=5 发送完组对成功后收到回复(组对完成) 7. groupstatus=6 收到组对通知后未答复
	 * 8. groupstatus=7 收到组对通知后已答复，等待组对完成 9. groupstatus=8 建组一方取消了建组 10.
	 * groupstatus=9 建组成功后抬笔的状态,和groupstatus=0一样，因为要求是组队完成后依然 可以组队(顶替现有的组对)
	 */
	public volatile int groupstatus = 0;// 组对状态是否发生
	private volatile boolean groupRequesting = false;// 组对请求是否发生
	private volatile boolean firstOccur = true;// 该变量用来判断一件事是否是第一次发生
	private volatile long lastOccurTime = 0;
	private boolean ismSmartPenStrokeBufferNeedClear = true;
	public GestureLibrary gestureLibrary = GestureLibraries.fromFile("/sdcard/zgmgesture");
	public int currentOIDSize = 0;
//手势相关变量
//private final ArrayList<MGesturePoint> mSmartPenStrokeBuffer = new ArrayList<MGesturePoint>();
	private final ArrayList<GesturePoint> SmartPenStrokeBuffer = new ArrayList<GesturePoint>();
//	public ArrayList<Position> temContair = new ArrayList<Position>();
	/*
	 * private MGesture mCurrentGesture=null; private Gesture currentGesture=null;
	 */
	private SmartPenGesture currentSmartPenGesture = null;
	private MGestureStroke currentMGestureStroke = null;
	private String penPower = "未连接智能笔";
	private String penName = "未连接智能笔";
	private String penAdress = "未连接智能笔";
	private String penPointInfString = "点信息显示区域";
	private String orderState = "还未书写手势";
	private String penStrokeCount = "0";
	private String GestureCounter="0";
	private int intPenStrokeCount = 0;
	public int  intGestureCount=0;
	public final int PENPOWER = 0;
	public final int PENNAME = 1;
	public final int PENADRESS = 2;
	public final int PENPOINTINF = 3;
	public final int ORDERSTATE = 4;
	public final int PENSTROKECOUNT = 5;
	public final int USERNAME = 6;
	public final int GESTURECOUNTER=7;
	private String usinginf = "<b>使&nbsp;&nbsp;&nbsp;&nbsp;用&nbsp;&nbsp;&nbsp;&nbsp;者 :</b>  " + mNameString + "<br/>"
			+ "<b>智能笔名称:</b>  " + penName + "<br/>" + "<b>智能笔地址:</b>  " + penAdress + "<br/>" + "<b>智能笔电量:</b>  "
			+ penPower + "<br/>" + "<b>动 作 状 态:</b>  " + orderState + "<br/>" + "<b>笔 迹 信 息:</b>  " + penPointInfString
			+ "<br/>" + "<b>书写笔迹数:</b>  " + penStrokeCount
			+ "<br/>" + "<b>书写指令数:</b>  " + intGestureCount;
	private int groupPeopleCount = 0;
	private int mygroupnumber = 0;
	public volatile boolean doSomeworkIsOK = true;
	public boolean isDealPenPoint = true;
	public boolean correcting = false;// 正在批改状态
	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	private String mConnectedDeviceName = null;
	// private ArrayAdapter<String> mConversationArrayAdapter;
	private StringBuffer mOutStringBuffer;

	// wsk 2019.5.7
	boolean RadioIsPlaying = false;

	public ArrayList<SmartPenPage> smartPenPageContainer=new ArrayList<SmartPenPage>();
	public ArrayList<String> smartPenPageNameContainer=new ArrayList<String>();
	WifiUtil wifiUtil = null;
	// The Handler that gets information back from the BluetoothChatService
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
//			final AlertDialog builder = new AlertDialog.Builder(MainActivity.this).create();
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothChatService.STATE_CONNECTED:
					// setStatus(getString(R.string.title_connected_to,
					// mConnectedDeviceName));
					// mConversationArrayAdapter.clear();
					break;
				case BluetoothChatService.STATE_CONNECTING:
					// setStatus(R.string.title_connecting);
					break;
				case BluetoothChatService.STATE_LISTEN:
				case BluetoothChatService.STATE_NONE:
					// setStatus(R.string.title_not_connected);
					break;
				}
				break;
			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				String writeMessage = new String(writeBuf);
				// mConversationArrayAdapter.add("Me: " + writeMessage);
				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;

				try {
					if (ObjAndByte.ByteToObject(readBuf).getClass().getName()
							.equalsIgnoreCase("com.tqltech.tqlpencomm.Dot")) {
						final Dot mdot = (Dot) ObjAndByte.ByteToObject(readBuf);
						mProcessDots(mdot);
						/* ProcessEachDot(mdot); */
//						ProcessDots(mdot);
						Log.e("zgm", "1201:" + mdot.ab_x);
					}
					if (ObjAndByte.ByteToObject(readBuf).getClass().getName()
							.equalsIgnoreCase("com.example.pencon.StringMessage")) {
						StringMessage mStringMessage = (StringMessage) ObjAndByte.ByteToObject(readBuf);
						otherNameString = mStringMessage.getMessageOwnerString();
						// ProcessDots(mdot);
						switch (mStringMessage.getPrefix()) {
						case GROUP_REQUEST:// 组队请求发起者发过来的信息
							groupstatus = 6;
							groupLeader = 0;// 其他人发送的组对请求
							otherGroupedNumber = Integer.parseInt(mStringMessage.getmessageString());
							Log.e("zgm", "1207:otherGroupedNumber:" + otherGroupedNumber);
							// 请求组对通知
							if (firstOccur) {
								firstOccur = false;
								showSound(R.raw.in);
								showVibrator();// 震动
								updateUsingInfo(otherNameString + "建立了小组，组对码是：" + otherGroupedNumber, ORDERSTATE);
								runOnUIThread("建组通知!", otherNameString + "建立了小组，组对码是：" + otherGroupedNumber);
								builder.show();
							}

							/*
							 * 下面的代码是请求对话框
							 */
							/*
							 * builder.setTitle("请求组对!"); builder.setMessage(messageOwnerString
							 * +"请求组对，组对码是："+otherGroupedNumber); builder.setButton
							 * (DialogInterface.BUTTON_POSITIVE,"同意", new DialogInterface.OnClickListener()
							 * {
							 * 
							 * @Override public void onClick(DialogInterface dialog, int which) {
							 * StringMessage mStringMessage = new StringMessage(GROUP_ANSWER,
							 * "1",mNameString); byte[] mbyte = ObjAndByte.ObjectToByte(mStringMessage);
							 * sendMessageManyTimes(mbyte); builder
							 * .setButton(DialogInterface.BUTTON_NEGATIVE,"拒绝!", new
							 * DialogInterface.OnClickListener() {
							 * 
							 * @Override public void onClick(DialogInterface dialog, int which) {
							 * StringMessage mStringMessage = new StringMessage(GROUP_ANSWER,
							 * "0",mNameString); byte[] mbyte = ObjAndByte.ObjectToByte(mStringMessage);
							 * sendMessageManyTimes(mbyte); builder.show();
							 */

							break;
						case GROUP_ANSWER:
							groupstatus = 5;
							if (firstOccur) {
								firstOccur = false;
								showSound(R.raw.in);
								showVibrator();// 震动
								if (groupLeader == 0) {
									runOnUIThread("组对状态通知!",
											mNameString + "和" + otherNameString + "组队成功！！！！您的成员类别是:组员");
									updateUsingInfo(mNameString + "和" + otherNameString + "组队成功！！！！您的成员类别是:组员",
											ORDERSTATE);
								} else {
									updateUsingInfo(mNameString + "和" + otherNameString + "组队成功！！！！您的成员类别是:组长",
											ORDERSTATE);
									updateUsingInfo(mNameString + "和" + otherNameString + "组队成功！！！！您的成员类别是:组长",
											ORDERSTATE);

								}
								dismissAlertDialog(builder);
							}
							// boolean
							// answerState=Boolean.parseBoolean(mStringMessage.getmessageString());
							// otherGroupedNumber =
							// Integer.parseInt(mStringMessage
							// .getmessageString());
							// if (otherGroupedNumber == groupedNumber) {
							// groupstatus = true;
							// }
							/*
							 * builder.setTitle("请求组对结果"); if(mStringMessage.getmessageString
							 * ().equals("0")){ builder.setMessage(mStringMessage
							 * .getMessageOwnerString()+"拒绝了您的请求"); builder.setButton
							 * (DialogInterface.BUTTON_POSITIVE,"知道了", new DialogInterface.OnClickListener()
							 * {
							 * 
							 * @Override public void onClick(DialogInterface dialog, int which) {
							 * 
							 * } });
							 * 
							 * builder.show();
							 * 
							 * } if(mStringMessage.getmessageString().equals("1") ){
							 * builder.setMessage(mStringMessage. getMessageOwnerString()+"接受了您的组对请求");
							 * builder.show();
							 * 
							 * new Handler().postDelayed(new Runnable() {
							 * 
							 * @Override public void run() { builder.dismiss(); } }, 1000);
							 * 
							 * }
							 */

							// haveRequest=1;
							break;
						case GROUP_CANCLE:
							if (groupstatus != 0) {
								groupstatus = 8;// 建组失败状态
							}

							if (firstOccur) {
								firstOccur = false;
								showSound(R.raw.in);
								showVibrator();// 震动
								updateUsingInfo(otherNameString + "提前抬笔中止了建组", ORDERSTATE);
//								runOnUIThread("组对状态通知!", otherNameString+"提前抬笔中止了建组")	;
								dismissAlertDialog(builder);
							}
							break;
						case HOMEWORK:
							showSound(R.raw.in);
							showVibrator();// 震动
							runOnUIThread("通知", otherNameString + "要批改你的作业");
							updateUsingInfo(otherNameString + "要批改你的作业", ORDERSTATE);
							dismissAlertDialog(builder);
							break;
						case NORMAL_Message:
							Log.e("zgm", "普通消息:" + mStringMessage.getmessageString());
							break;
						case DISSOLVE_GROUP:
							groupstatus = 0;// 其他人发送的组对请求
							groupLeader = 1;
							if (firstOccur) {
								firstOccur = false;
								showSound(R.raw.in);
								showVibrator();// 震动
								updateUsingInfo(otherNameString + "解散了小组", ORDERSTATE);
//								 runOnUIThread("组对状态通知!", otherNameString+"解散了小组");
							}
							break;
						case GROUP_ALREADY:// 其他组员笔点击在点读码上后发过来的消息
							groupstatus = 2;// 发送完组对通知后得到接受组对的答复
							otherGroupedNumber = Integer.parseInt(mStringMessage.getmessageString());
							if (firstOccur) {
								firstOccur = false;
								showSound(R.raw.in);
								showVibrator();// 震动
								updateUsingInfo(mStringMessage.getMessageOwnerString() + "已点击了组对码", ORDERSTATE);
								runOnUIThread("组对状态通知!", mStringMessage.getMessageOwnerString() + "点击了组对码:"
										+ mStringMessage.getmessageString());

							}
							break;
						case GROUP_NUMBER_WRONG:// 表示和组对发起者的组对码不相等

							break;
						case GROUP_SUCESS:
//							groupInit();
							mStringMessage = new StringMessage(GROUP_ANSWER, "", mNameString);
							byte[] mbyte = ObjAndByte.ObjectToByte(mStringMessage);
							sendMessageManyTimes(mbyte);
							groupstatus = 5;
							if (firstOccur) {
								firstOccur = false;
								showSound(R.raw.in);
								showVibrator();// 震动
								if (groupLeader == 0) {
									updateUsingInfo("C", USERNAME);
									initGroupPeople();
									groupPeople2.setBackgroundColor(Color.GREEN);
									runOnUIThread("组对状态通知!",
											mNameString + "和" + otherNameString + "组队成功！！！！您的成员类别是:组员");
									updateUsingInfo(mNameString + "和" + otherNameString + "组队成功！！！！您的成员类别是:组员",
											ORDERSTATE);
									/*
									 * updateUsingInfo(mNameString+"和"+otherNameString+"组队成功！！！！您的成员类别是:组员",
									 * ORDERSTATE);
									 */
									;
								} else {
									updateUsingInfo("A", USERNAME);
									initGroupPeople();
									groupPeople1.setBackgroundColor(Color.GREEN);
									updateUsingInfo(mNameString + "和" + "C" + "组队成功！！！！您的成员类别是:组长", ORDERSTATE);
									/* updateUsingInfo(mNameString+"和"+"C"+"组队成功！！！！您的成员类别是:组长",ORDERSTATE); */
								}
								dismissAlertDialog(builder);

							}
							break;
						case 410:// 通过UDP发来的信息
							groupPeopleCount++;
							udpSocket.sendMessage(
									ObjAndByte.ObjectToByte(new StringMessage(4101, groupPeopleCount + "")),
									mStringMessage.getmessageString());
							break;
						case 4101:// 通过UDP发来的信息
							mgroupedNumber = Integer.parseInt(mStringMessage.getmessageString());
							switch (mgroupedNumber) {
							case 1:
								updateUsingInfo("组长1号", USERNAME);
								break;
							case 2:
								updateUsingInfo("组员2号", USERNAME);
								break;
							case 3:
								updateUsingInfo("组员3号", USERNAME);
								break;
							case 4:
								updateUsingInfo("组员4号", USERNAME);
								break;

							default:
								break;
							}
							break;

						default:

							break;
						}

						Log.e("zgm", "1201:点读码是：" + mStringMessage.getmessageString());
					}

				} catch (Exception e) {
					// TODO: handle exception
					Log.e("zgm", " " + e);

				}
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				showSound(R.raw.in);
				showVibrator();// 震动
				Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT)
						.show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
				break;
			case 0417:// 订正状态，将老师批改的文件加载出来
				filenames = iteratorPath("/sdcard/xyz", studentNumber, gCurBookID, gCurPageID);
				doDrawFromFile();
				break;
			}
		}
	};
	public int homeworkStatus = 1;// 0-其他状态， 1-作答状态，2-批改状态，3-订正状态

	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		public void onServiceConnected(final ComponentName className, IBinder rawBinder) {
//			updateUsingInfo(penName, PENNAME);
			mService = ((BluetoothLEService.LocalBinder) rawBinder).getService();
			Log.d("mService:", "onServiceConnected mService= " + mService);
			if (!mService.initialize()) {
				finish();
			}
			penName = ApplicationResources.mPenName + "";
			mService.setOnDataReceiveListener(new BluetoothLEService.OnDataReceiveListener() {

				@Override
				public void onDataReceive(final Dot dot) {
					if (!isDealPenPoint) {
						return;
					}
					if (gCurBookID != dot.BookID || gCurPageID != dot.PageID) {
						String sqlstr = "";
						gCurBookID = dot.BookID;
						gCurPageID = dot.PageID;
						showSound(R.raw.in);
						switch (homeworkStatus) {

						case 0:// 其他状态
//							Log.e("zgm", "homeworkStatus="+homeworkStatus+"其他状态");
							break;
						case 1:// 作答状态，默认//此时要更新pageid,bookid,和学号的对应关系并保存在数据库中
//							Log.e("zgm", "homeworkStatus="+homeworkStatus+"作答状态");
							/*
							 * sqlstr="REPLACE INTO homeworkpageandstudentinfo (bookid,pageid,studentNumber) VALUES (?,?,?)"
							 * ; Object[] args = new Object[]
							 * {dot.BookID+"",dot.PageID+"",studentNumber+""}; try{
							 * 
							 * smartPenDatabase.execSQL(sqlstr,args);
							 * 
							 * } catch (SQLException ex) { Log.e("zgm",
							 * "0117:ex.getMessage():"+ex.getMessage()); }
							 */
							break;
						case 2:// 作业正在批改状态
//							Log.e("zgm", "homeworkStatus="+homeworkStatus+"批改状态");

							break;
						case 3:// 订正状态，此时要根据pageid,bookid来从数据库中找学号
//							Log.e("zgm", "homeworkStatus="+homeworkStatus+"订正状态");
							/*
							 * mHandler.sendEmptyMessage(0417);//message.what=0417; Log.e("zgm",
							 * "homeworkStatus="+homeworkStatus+"订正状态");
							 * sqlstr="SELECT studentNumber FROM homeworkpageandstudentinfo WHERE bookid=? AND pageid=?"
							 * ; try{ Cursor mCursor =smartPenDatabase.rawQuery(sqlstr,new
							 * String[]{dot.BookID+"",dot.PageID+""}); if (mCursor.getCount()==0)
							 * {//没有找到信息，那么就自动进入作答状态 homeworkStatus=1;
							 * gCurBookID=-1;//为了强制进入homeworkStatus=1的作答状态 return; }else {
							 * mCursor.moveToFirst();
							 * studentNumber=mCursor.getString(mCursor.getColumnIndex("studentNumber"));
							 * Log.e("zgm",
							 * "student:"+studentNumber+" dot.BookID:"+dot.BookID+" dot.PageID:"+dot.PageID)
							 * ;
							 *//**
								 * 下载文件的操作
								 */
							/*
							 * // showDownLoadDialog();
							 * 
							 * // mHandler.sendEmptyMessage(0417);//message.what=0417;
							 *//**
								 * 重画图操作
								 *//*
									 * } } catch (SQLException ex) { Log.e("zgm",
									 * "0117:ex.getMessage():"+ex.getMessage()); return ; }
									 */
							break;
						default:
							break;
						}
					}
					if (dot.type == DotType.PEN_DOWN) {
						g_x0 = (float) (dot.x + dot.fx / 100.0);
						g_y0 = (float) (dot.y + dot.fy / 100.0);

					}

					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							/*
							 * Log.i("zgm", "Dot信息,BookID" + dot.BookID); Log.i("zgm", "Dot信息,ab_x" +
							 * dot.ab_x);
							 */

							if (!correcting) {// 不是小组互批状态
								ProcessDots(dot);
							}
							if (groupstatus == 9 && correcting) {// 如果组队成功，那么将数据发送 { byte[]

								byte[] dotByte = ObjAndByte.ObjectToByte(dot);
								/*
								 * if (dot.type==Dot.DotType.PEN_DOWN) { sendMessageManyTimes(dotByte); }
								 */
								sendMessageManyTimes(dotByte);
							}

						}
					});
				}

				@Override
				public void onOfflineDataReceive(final Dot dot) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							ProcessDots(dot);
						}
					});
				}

				@Override
				public void onFinishedOfflineDown(boolean success) {
					// Log.i(TAG, "---------onFinishedOfflineDown--------" +
					// success);
					/*
					 * layout.setVisibility(View.GONE); bar.setProgress(0);
					 */
				}

				@Override
				public void onOfflineDataNum(final int num) {
					// Log.i(TAG, "---------onOfflineDataNum1--------" + num);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									// textView.setText("离线数量有" +
									// Integer.toString(num * 10) + "bytes");
									/*
									 * 
									 * //if (num == 0) { // return; //}
									 * 
									 * Log.e("zgm","R.id.dialog1"+R.id.dialog); dialog =
									 * (RelativeLayout)findViewById(R.id .dialog);
									 * Log.e("zgm","dialog"+dialog.getId()); dialog.setVisibility(View.VISIBLE);
									 * textView = (TextView) findViewById(R.id.textView2); textView.setText("离线数量有"
									 * + Integer.toString(num * 10) + "bytes"); confirmBtn = (Button)
									 * findViewById(R.id.but); confirmBtn.setOnClickListener(new
									 * View.OnClickListener() {
									 * 
									 * @Override public void onClick(View view) { dialog.setVisibility(View.GONE); }
									 * });
									 */
								}

							});
						}
					});
				}

				@Override
				public void onReceiveOIDSize(final int OIDSize) {// 这里接收点读数据
					if (!isDealPenPoint) {
						return;
					}
					if (OIDSize > 20000 && OIDSize < 40000) {
						if (currentOIDSize != OIDSize) {
							ReadVideo(OIDSize);
							currentOIDSize = OIDSize;
							return;
						} else {
							return;
						}
					}
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (groupstatus == 0)// 未发生组队状态
							{
								if (wifiUtil == null) {
									wifiUtil = new WifiUtil(MainActivity.this);
								}
								if (groupedNumber == 1) {
									boolean connected = wifiUtil.createWifiAp("hha" + OIDSize, "hha" + OIDSize);
									if (connected) {
										groupstatus = 1;
										updateUsingInfo("创建网络" + "hha" + OIDSize, ORDERSTATE);
										showSound(R.raw.in);
									} else {
										updateUsingInfo("创建网络" + "hha" + OIDSize + "失败", ORDERSTATE);
									}
								} else {
									boolean connected = wifiUtil.connectWifi("hha" + OIDSize, "hha" + OIDSize,
											WifiCipherType.WIFICIPHER_WPA);
									if (connected) {
										groupstatus = 1;
										updateUsingInfo("加入网络" + "hha" + OIDSize, ORDERSTATE);
										showSound(R.raw.in);
										wifiUtil.getServerIPAddress();
										udpSocket.startUDPSocket();
										StringMessage stringMessage = new StringMessage(410,
												wifiUtil.getLocalIPAddress());// 410表示请求发分配组员编号
										byte[] bytes = ObjAndByte.ObjectToByte(mStringMessage);
										udpSocket.sendMessage(bytes, wifiUtil.getServerIPAddress());

									} else {
										updateUsingInfo("加入网络" + "hha" + OIDSize + "失败", ORDERSTATE);
									}

								}
							} else {
								updateUsingInfo("点读操作：点读码是" + OIDSize, ORDERSTATE);
							}
							/*
							 * boolean connected=wifiUtil.connectWifi("hha"+OIDSize,
							 * "hha"+OIDSize,WifiCipherType.WIFICIPHER_WPA); if (connected) {
							 * updateUsingInfo("加入网络"+"hha"+OIDSize, ORDERSTATE); } if (!connected) {
							 * wifiUtil.createWifiAp("hha"+OIDSize,"hha"+OIDSize);
							 * updateUsingInfo("创建网络"+"hha"+OIDSize, ORDERSTATE); }
							 * 
							 */ // joinOrCreateGroup(OIDSize);
						}
					});
				}

				@Override
				public void onReceiveOfflineProgress(final int i) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							/*
							 * if (startOffline) {
							 * 
							 * layout.setVisibility(View.VISIBLE); text.setText("开始缓存离线数据");
							 * bar.setProgress(i); Log.e(TAG, "onReceiveOfflineProgress----" + i); if (i ==
							 * 100) { layout.setVisibility(View.GONE); bar.setProgress(0); } } else {
							 * layout.setVisibility(View.GONE); bar.setProgress(0); }
							 */
						}

					});
				}

				@Override
				public void onDownloadOfflineProgress(final int i) {

				}

				@Override
				public void onReceivePenLED(final byte color) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Log.e(TAG, "receive led is " + color);
							switch (color) {
							case 1: // blue
								gColor = 5;
								break;
							case 2: // green
								gColor = 3;
								break;
							case 3: // cyan
								gColor = 8;
								break;
							case 4: // red
								gColor = 1;
								break;
							case 5: // magenta
								gColor = 7;
								break;
							case 6: // yellow
								gColor = 2;
								break;
							case 7: // white
								gColor = 6;
								break;
							default:
								break;
							}
						}
					});
				}

				@Override
				public void onOfflineDataNumCmdResult(boolean success) {
					// Log.i(TAG, "onOfflineDataNumCmdResult---------->" +
					// success);
				}

				@Override
				public void onDownOfflineDataCmdResult(boolean success) {
					// Log.i(TAG, "onDownOfflineDataCmdResult---------->" +
					// success);
				}

				@Override
				public void onWriteCmdResult(int code) {
					// Log.i(TAG, "onWriteCmdResult---------->" + code);
				}

				@Override
				public void onReceivePenType(int type) {
					// Log.i(TAG, "onReceivePenType type---------->" + type);
					penType = type;
				}
			});
		}

		public void onServiceDisconnected(ComponentName classname) {
			showSound(R.raw.smartpemdisconnect);
			mService = null;
		}
	};

	private BluetoothAdapter tableBluetoothAdapter;
	private volatile long penUpTime;
	private volatile boolean firstpen = true;
	protected volatile long curTime;
	protected volatile long soundTime;
	private volatile boolean firstPenChi = true;
	public MediaPlayer mediaPlayer = null;
	DisplayMetrics dm;
	public UDPSocket udpSocket = null;
	private int count = 0;
	private int showInfoViewCouter = 0;
	int offset = 0;
	public TextView groupPeople1;
	public TextView groupPeople2;
//	private String inputIp="192.168.1.113";
	private String inputIp = "123.206.16.114";
	private SQLiteDatabase smartPenDatabase;

	LinearLayout smartPenInforLinearLayout;
	LinearLayout groupInforLinearLayout;
	LinearLayout gestureInforLinearLayout;

	public void drawInit() {

		bDrawl[0].initDraw();
		bDrawl[0].setVcolor(Color.WHITE);
		bDrawl[0].setVwidth(1);

		SetPenColor(gColor);
		bDrawl[0].paint.setStrokeCap(Paint.Cap.ROUND);
		bDrawl[0].paint.setStyle(Paint.Style.FILL);
		bDrawl[0].paint.setAntiAlias(true);
		bDrawl[0].invalidate();

	}

	public void SetPenColor(int ColorIndex) {
		switch (ColorIndex) {
		case 0:
			bDrawl[0].paint.setColor(Color.GRAY);
			return;
		case 1:
			bDrawl[0].paint.setColor(Color.RED);
			return;
		case 2:
			bDrawl[0].paint.setColor(Color.rgb(192, 192, 0));
			return;
		case 3:
			bDrawl[0].paint.setColor(Color.rgb(0, 128, 0));
			return;
		case 4:
			bDrawl[0].paint.setColor(Color.rgb(0, 0, 192));
			return;
		case 5:
			bDrawl[0].paint.setColor(Color.BLUE);
			return;
		case 6:
			bDrawl[0].paint.setColor(Color.BLACK);
			return;
		case 7:
			bDrawl[0].paint.setColor(Color.MAGENTA);
			return;
		case 8:
			bDrawl[0].paint.setColor(Color.CYAN);
			return;
		}
		return;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.newmainformi4);
		setContentView(R.layout.newmainforwatchsimple);
		dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		mWidth = dm.widthPixels;
		mHeight = dm.heightPixels;

		float density = dm.density; // 屏幕密度（0.75 / 1.0 / 1.5）
		int densityDpi = dm.densityDpi; // 屏幕密度dpi（120 / 160 / 240）
		Log.e(TAG, "density=======>" + density + ",densityDpi=======>" + densityDpi);
		// 屏幕宽度算法:屏幕宽度（像素）/屏幕密度
		int screenWidth = (int) (mWidth / density); // 屏幕宽度(dp)
		int screenHeight = (int) (mHeight / density);// 屏幕高度(dp)
		Log.e(TAG, "width=======>" + screenWidth);
		Log.e(TAG, "height=======>" + screenHeight);

		Log.e(TAG, "-----screen pixel-----width:" + mWidth + ",height:" + mHeight);
		smartPenInforLinearLayout = (LinearLayout) findViewById(R.id.dynamic_debug_area);
		groupInforLinearLayout = (LinearLayout) findViewById(R.id.group_dynamic_debug_area);
		gestureInforLinearLayout = (LinearLayout) findViewById(R.id.backstage_dynamic_debug_area);

		dealSmartPenGesture.setDealSmartPenGesture(this);
		builder = new AlertDialog.Builder(MainActivity.this).create();
//		builder= new AlertDialog.Builder(MainActivity.this).create();
		// setContentView(R.layout.draw);
		// textView=findViewById(R.id.maintextview);
//		setContentView(R.layout.activity_main);
		bDrawl[0] = new DrawView(this);
//     		bDrawl[0].setBackgroundResource(R.drawable.pagebg);
		bDrawl[0].setVcolor(Color.YELLOW);
	/*	ImageView pencagebcackground = (ImageView) findViewById(R.id.pencage_background);
		pencagebcackground.setBackgroundResource(R.drawable.pencageformi4);*/
//		ScrollView scrollView=(ScrollView) findViewById(R.id.scroll);
		/*
		 * ScrollView scrollView=new ScrollView(this); scrollView.addView(bDrawl[0]);
		 */
//		penChirography = (LinearLayout) findViewById(R.id.pen_chirography);
//		LinearLayout.LayoutParams bwLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
//				LayoutParams.WRAP_CONTENT);
//		penChirography.addView(bDrawl[0], bwLayoutParams);
		showInftTextView = (TextView) findViewById(R.id.maintextview);
//			showInftTextView.setTextSize(20);
		showInftTextView.setText(Html.fromHtml(usinginf));
		drawInit();
		groupInit();
		informationLayout = (RelativeLayout) findViewById(R.id.information);
		pencage = (RelativeLayout) findViewById(R.id.pencage);
		pen_chirography_all = (LinearLayout) findViewById(R.id.pen_chirography_all);
		/* groupinfo=(ImageView) findViewById(R.id.grouptextview); */
		gestureinfo = (TextView) findViewById(R.id.backstage_gestureinfo);
		penStatus = (TextView) findViewById(R.id.pen_status);
		groupPeople1 = (TextView) findViewById(R.id.group_people1);
		groupPeople2 = (TextView) findViewById(R.id.group_people2);
		penStatus.setMovementMethod(ScrollingMovementMethod.getInstance());

		tableBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();// 非低功耗蓝牙适配器

		// If the adapter is null, then Bluetooth is not supported
		if (mChatService == null)
			setupChat();
		if (tableBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		{// 低功耗蓝牙
			Intent gattServiceIntent = new Intent(this, BluetoothLEService.class);
			boolean bBind = bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
		}

			A5_X_OFFSET = 0;
		A5_Y_OFFSET = 0;
		udpSocket = new UDPSocket(this);
		/*
		 * wifiUtil=new WifiUtil(MainActivity.this);
		 * wifiUtil.createWifiAp("hha123456","hha123456");
		 */
		if (!IsTableExist("/sdcard/smartPenv190411.db", "homeworkpageandstudentinfo")) {// 打开或创建数据库，并判断表是否存在
			smartPenDatabase
					.execSQL("create table homeworkpageandstudentinfo(" + "id integer primary key autoincrement,"
							+ "bookid char[20]," + "pageid char[20]," + "studentNumber char[20])");// 学生学号
		}
	}

	protected void onPause() {
		SmartPenUnitils.save(smartPenPage);
		super.onPause();
	}

	protected void onDestroy() {
		unbindService(dealSmartPenGesture.recordConnection);
		SmartPenUnitils.save(smartPenPage);
		wifiUtil.closeWifiAp();
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		// gImageView = (ImageView)
		// findViewById(R.id.imageView2);//得到ImageView对象的引用
		// gImageView.setScaleType(ImageView.ScaleType.FIT_XY);

		// 计算
		float ratio = 1f;
		ratio = (float) ((ratio * mWidth) / A5_BG_REAL_WIDTH);
		BG_WIDTH = (int) (A5_BG_REAL_WIDTH * ratio);
		BG_HEIGHT = (int) (A5_BG_REAL_HEIGHT * ratio);

		// gcontentLeft =
		// getWindow().findViewById(Window.ID_ANDROID_CONTENT).getLeft();
		// gcontentTop =
		// getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();

		// A5_X_OFFSET = (int) (mWidth - gcontentLeft - BG_WIDTH) / 2;
		// A5_Y_OFFSET = (int) (mHeight - gcontentTop - BG_HEIGHT) / 2;
		// mHandler.sendEmptyMessage(UPDATE_UI_OFFSET);

		RunReplay();

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		bleManager = PenCommAgent.GetInstance(getApplication());// 低功耗蓝牙(智能笔)初始化
		bleManager.setPenBeepMode(true);
		// 0-free format;1-for A4;2-for A3
		// Log.i(TAG, "-----------setDataFormat-------------");
//		bleManager.setXYDataFormat(1);// 设置码点输出规格

		switch (item.getItemId()) {

		case R.id.action_settings:
			// Launch the DeviceListActivity to see devices and do scan
			serverIntent = new Intent(this, SelectDeviceActivity.class);
			startActivityForResult(serverIntent, REQUEST_SELECT_DEVICE);
			return true;
		case R.id.scan_bluetooth:
			// Launch the DeviceListActivity to see devices and do scan
			serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
			setupChat();
			return true;
		case R.id.dissolve_group:
			// Launch the DeviceListActivity to see devices and do scan
			groupInit();
			updateUsingInfo("小组解散", ORDERSTATE);
			StringMessage mStringMessage = new StringMessage(DISSOLVE_GROUP, "", mNameString);
			byte[] mbyte = ObjAndByte.ObjectToByte(mStringMessage);
			showVibrator();// 震动
			groupInit();
			sendMessageManyTimes(mbyte);

			return true;
		case R.id.change_uer_name:
			// Launch the DeviceListActivity to see devices and do scan
			// 通过AlertDialog.Builder这个类来实例化我们的一个AlertDialog的对象
			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
			// builder.setIcon(R.drawable.ic_launcher);
			// 设置Title的内容
			builder.setTitle("请输入您的名字");
			final EditText userNameTextView = new EditText(this);
			// 设置我们自己定义的布局文件作为弹出框的Content
			builder.setView(userNameTextView);
			builder.setPositiveButton("完成", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mNameString = userNameTextView.getText().toString().trim();
					updateUsingInfo(mNameString, USERNAME);
					// 将输入的用户名和密码打印出来
					Log.e("zgm", "hhah:" + mNameString);
				}
			});
			builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			});
			builder.show();
			return true;
		case R.id.save_gesture:
			if (currentSmartPenGesture == null) {
				return true;
			}
			saveGesture(currentSmartPenGesture);
			return true;
		case R.id.recognize_gesture:
			if (currentSmartPenGesture == null) {
				return true;
			}
			recognizeGesture(currentSmartPenGesture);
			return true;
		case R.id.openpencageactivity:
			/*
			 * Intent intent = new Intent(this, PencageActivity.class);
			 * startActivity(intent);
			 */
//			penChirography.setBackgroundResource(R.drawable.bg);
			showSound(R.raw.in);
//			Toast.makeText(getBaseContext(), "请订正", Toast.LENGTH_SHORT);
			homeworkStatus = 3;
			showSound(R.raw.in);
			doDownLoadWork();
			return true;
		case R.id.read_from_file:
			String filename = "123";
//			drawsmartpenpoints(getfromFile("/sdcard/xyz/"+filename+".page",filename+".page"));
			SetPenColor(1);
			drawsmartpenpoints(getfromFile("/sdcard/xyz/" + filename + "-1.page", filename + "-1.page"));
			return true;
		case R.id.save_chirography:
			SmartPenUnitils.save(smartPenPage);
//			showSound(R.raw.in);
			Toast.makeText(getBaseContext(), "保存成功", Toast.LENGTH_SHORT).show();

			new Thread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					boolean statsu = UpLoad.uploadFile(
							"http://" + inputIp + "/jxyv1/index.php/Home/Index/smart_pen_upload",
							"/sdcard/-1/" + "123.page");
					if (statsu) {
						showSound(R.raw.upload_sucess);
//					Toast.makeText(getBaseContext(), "上传成功", Toast.LENGTH_SHORT).show();
					} else {
						showSound(R.raw.upload_fail);
//					Toast.makeText(getBaseContext(), "上传失败", Toast.LENGTH_SHORT).show();
					}
				}
			}).start();

			return true;
		case R.id.clean:
			deleteDir("/sdcard/-1/");
			deleteDir("/sdcard/xyz/");
			smartPenPage = null;
			System.gc();
			break;
		case R.id.exitwatch:
			informationLayout.setVisibility(View.VISIBLE);
			pen_chirography_all.setVisibility(View.VISIBLE);
			pencage.setVisibility(View.VISIBLE);
//			initMiniShowAreaVisibility();
			minishowlayout.setVisibility(View.GONE);
			System.gc();
			break;

		}

		return false;
	}

	/*
	 * 将原始点数据进行处理（主要是坐标变换）并保存
	 */

	private void ProcessEachDot(Dot dot) {
		penUpTime = System.currentTimeMillis();
		// float ratio = 0.95f;
		float ratio = 0.95f;

		if (dot.y > 185) {
			if (mediaPlayer != null && mediaPlayer.isPlaying()) {
				mediaPlayer.stop();
			}
			RadioIsPlaying = true;
		}
		float A_45_ratio = (float) (A5_WIDTH / A4_WIDTH);
//		Log.e("zgm", "A5_WIDTH/A4_WIDTH=" + (A5_WIDTH / A4_WIDTH));
		float ax = (float) (A5_WIDTH / XDIST_PERUNIT); // A5纸张宽度方向包含多少个编码单元
		float ay = (float) (A5_HEIGHT / YDIST_PERUNIT);

		Log.i("zgm", "111 ProcessEachDot=" + dot.toString());

		/*
		 * if (dot.BookID != gCurBookID || dot.PageID != gCurPageID) {
		 * 
		 * }
		 */
		/*
		 * if (dot.BookID == 100) {
		 * 
		 * ratio = (float) ((ratio * mWidth) / A5_BG_REAL_WIDTH); BG_WIDTH = (int)
		 * (A5_BG_REAL_WIDTH * ratio); BG_HEIGHT = (int) (A5_BG_REAL_HEIGHT * ratio);
		 * 
		 * ax = (float) (A5_WIDTH / XDIST_PERUNIT); ay = (float) (A5_HEIGHT /
		 * YDIST_PERUNIT); Log.e("zgm", "book.id" + dot.BookID + " A5_BG_REAL_WIDTH:" +
		 * A5_BG_REAL_WIDTH);
		 * 
		 * }
		 */

//		if (dot.BookID == 0) {

		ratio = (float) ((ratio * mWidth) / A4_BG_REAL_WIDTH);
		BG_WIDTH = (int) (A4_BG_REAL_WIDTH * ratio);
		BG_HEIGHT = (int) (A4_BG_REAL_HEIGHT * ratio);
		/*
		 * //115是B5每页上的最大横坐标，133是A4每页上的最大横坐标
		 * 因为相当苦逼的是我发现A4纸上的最大横坐标和B5纸上的最大横坐标的比值不等于A4纸宽度于B5纸的宽度
		 * ，然而B5纸能够很好映射到屏幕，对A4纸只有乘以其坐标和B5坐标的比值，进行缩放了
		 */
		ax = (float) ((float) (A5_WIDTH / XDIST_PERUNIT) * (133.0 / 115));
		ay = (float) ((float) (A5_HEIGHT / YDIST_PERUNIT) * (133.0 / 115));
//			Log.e("zgm", "book.id" + dot.BookID + " BG_WIDTH:" + BG_WIDTH);
//		}
//		Log.i(TAG, "111 ProcessEachDot=" + dot.toString());

		int counter = 0;
		pointZ = dot.force;
		counter = dot.Counter;
		/*
		 * Log.i("zgm", "BookID:  " + dot.BookID); Log.i("zgm", "Counter: " +
		 * dot.Counter); Log.i("zgm", "Counter: " + dot.force);
		 */
		if (pointZ < 0) {
			// Log.i(TAG, "Counter=" + counter + ", Pressure=" + pointZ +
			// " Cut!!!!!");
			return;
		}
		int tmpx = dot.x;
		pointX = dot.fx;
		pointX /= 100.0;
		pointX += tmpx;

		int tmpy = dot.y;
		pointY = dot.fy;
		pointY /= 100.0;
		pointY += tmpy;
		gpointX = pointX;
		gpointY = pointY;
		// ax = (float) (A5_WIDTH / XDIST_PERUNIT);
		// float ay = (float) (A5_HEIGHT / YDIST_PERUNIT);
		pointX *= (BG_WIDTH);
		pointX /= ax;

		pointY *= (BG_HEIGHT);

//		Log.e("zgm", "BG_WIDTH:" + BG_WIDTH + " " + "BG_HEIGHT:" + BG_HEIGHT);
		pointY /= ay;
		/*
		 * pointX *= 0.65; pointX *= 0.65 pointY *= 0.65;
		 */
		pointX -= 20;
		pointY -= 32;
		pointX *= 1.48;
		pointY *= 1.48;
		/*
		 * pointX=(float) (pointX/2.0*1.03); pointY=(float) (pointY/2.0*1.03);
		 */
		/*
		 * pointX += A5_X_OFFSET; pointY += A5_Y_OFFSET;
		 */

		/*
		 * {//该代码块是为了屏幕上的penStatus能够随着 内容的增加自动滚动 offset = penStatus.getLineCount() *
		 * penStatus.getLineHeight(); if (offset > penStatus.getHeight()) {
		 * penStatus.scrollTo(0, offset - penStatus.getHeight() + 3 *
		 * penStatus.getLineHeight()); } }
		 */// 该代码块是为了屏幕上的penStatus能够随着内容的增加自动滚动
		/*
		 * penStatus.append("x:" + pointX + "   " + "y:" + pointY + "   " + "OwnerId:" +
		 * dot.OwnerID + "   " + "bookid:" + dot.BookID + "   " + "page:" + dot.PageID +
		 * "   " + "Section:" + dot.SectionID + "   " + "Section:" + dot.SectionID +
		 * "   " + "type:" + dot.type + "   " +"time:" + dot.timelong + "\n");
		 */
		/*
		 * penStatus.append("x:" + (dot.x+dot.fx/100.0) + "   " + "y:" +
		 * (dot.y+dot.fy/100.0) + "   " + "OwnerId:" + dot.OwnerID + "   " + "bookid:" +
		 * dot.BookID + "   " + "page:" + dot.PageID + "   " + "Section:" +
		 * dot.SectionID + "   " + "Section:" + dot.SectionID + "   " + "type:" +
		 * dot.type + "   " +"time:" + dot.timelong + "\n");
		 */
//		
//		Log.e("zgm", "ax:" + ax + " " + "ay:" + ay);
//		Log.e("zgm", "A5_X_OFFSET:" + A5_X_OFFSET + " " + "A5_Y_OFFSET:" + A5_Y_OFFSET);
		/*
		 * if (isSaveLog) { saveOutDotLog(dot.BookID, dot.PageID, pointX, pointY,
		 * dot.force, 1, gWidth, gColor, dot.Counter, dot.angle); }
		 */
		if (pointZ > 0) {
			if (dot.type == Dot.DotType.PEN_DOWN) {
//				penUpTime = System.currentTimeMillis();
				if (dot.PageID != dealSmartPenGesture.PageID) {
//					Log.e("zgm", "dot.BookID:" + dot.BookID + " " + "gCurBookID" + gCurBookID);
					bDrawl[0].canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);// 清除画布
					gCurBookID = dot.BookID;
					gCurPageID = dot.PageID;
					/*
					 * switch (dot.PageID) { case 0: //
					 * bDrawl[0].setBackgroundResource(R.drawable.pagebg); //
					 * bDrawl[0].bitmap=((BitmapDrawable)getResources().getDrawable(R.drawable.
					 * pagebg)).getBitmap();
					 * bDrawl[0].canvas.drawBitmap(((BitmapDrawable)getResources().getDrawable(R.
					 * drawable.pagebg)).getBitmap(), 0, 0, null); bDrawl[0].paint.setXfermode(new
					 * PorterDuffXfermode(Mode.SRC_IN)); break; case 1: //
					 * bDrawl[0].bitmap=((BitmapDrawable)
					 * getResources().getDrawable(R.drawable.pagebg1)).getBitmap(); //
					 * bDrawl[0].setBackgroundResource(R.drawable.pagebg1); //
					 * bDrawl[0].paint.setXfermode(new PorterDuffXfermode(Mode.OVERLAY));
					 * bDrawl[0].canvas.drawBitmap(((BitmapDrawable)
					 * getResources().getDrawable(R.drawable.pagebg1)).getBitmap(), 0, 0, null);
					 * bDrawl[0].paint.setXfermode(new PorterDuffXfermode(Mode.SRC)); break;
					 * default: break; }
					 */ dealSmartPenGesture.setPageID(dot.PageID);// 当前的页码和书写的页码不对
				}

				/**
				 * 画布显示设置
				 *
				 */
//				bDrawl[0].canvas.saveLayer(0, 0,bDrawl[0].bitmap.getWidth() ,bDrawl[0].bitmap.getHeight() , bDrawl[0].paint, Canvas.ALL_SAVE_FLAG);		
//				bDrawl[0].canvas.translate(0, 0);
//				bDrawl[0].paint.setXfermode(new PorterDuffXfermode(Mode.OVERLAY));		

//				将点坐标放进gestureBuffer中
//				showSound(R.raw.cricket);
				if (ismSmartPenStrokeBufferNeedClear) {
					SmartPenStrokeBuffer.clear();
//					temContair.clear();
//					Log.e("zgm", "1223:mSmartPenStrokeBufferisempty=" + SmartPenStrokeBuffer.isEmpty());

					/*
					 * if (currentSmartPenGesture!=null) {
					 * currentSmartPenGesture.SmartPenGestureClearAllStroke();
					 * currentSmartPenGesture.SmartPenGestureClearmBoundingBox(); }
					 */
//					Log.e("zgm","1223:mCurrentGesture.SmartPenGestureClearmBoundingBox()="+mCurrentGesture.getBoundingBox().height());
//					Log.e("zgm", "1210:mCurrentGesture.getStrokesCount()::"+mCurrentGesture.getStrokesCount());
				}

				SmartPenStrokeBuffer.add(new GesturePoint((float) (dot.x + dot.fx / 100.0),
						(float) (dot.y + dot.fy / 100.0), System.currentTimeMillis()));
//				temContair.add(new Position((float) (dot.x + dot.fx / 100.0), (float) (dot.y + dot.fy / 100.0)));
//				SmartPenStrokeBuffer.add(new GesturePoint(pointX, pointY, System.currentTimeMillis()));
//				mSmartPenStrokeBuffer.add(new MGesturePoint(pointX,pointY,System.currentTimeMillis()));
//				将点坐标放进gestureBuffer中 完				
//				bleManager.setPenBeepMode(true);
				/* Log.i("zgm", "20181128:主动打开蜂鸣器"); */
				// Log.i(TAG, "PEN_DOWN");
				gPIndex = 0;
				int PageID, BookID;
				PageID = dot.PageID;
				BookID = dot.BookID;
				firstPointX = pointX;
				firstPointY = pointY;
				firstPointTime = dot.timelong;
				/*
				 * Log.e("zgm", "dot.timelong:" + dot.timelong); Log.e("zgm", "dot.timelong:" +
				 * dot.timelong);
				 */
				if (PageID < 0 || BookID < 0) {
					// 谨防笔连接不切页的情况
					return;
				}
//				updateUsingInfo("x坐标:" + dot.x + "   " + "y坐标:" + dot.y,PENPOINTINF);
				// Log.i(TAG, "PageID=" + PageID + ",gCurPageID=" + gCurPageID +
				// ",BookID=" + BookID + ",gCurBookID=" + gCurBookID);
				/*
				 * 下面的判断bookid和pageid不想等的代码运行不到，也没用
				 */
				/*
				 * if (PageID != gCurPageID || BookID != gCurBookID) { gbSetNormal = false;
				 * SetBackgroundImage(BookID, PageID); //
				 * gImageView.setVisibility(View.VISIBLE); bIsOfficeLine = true; gCurPageID =
				 * PageID; gCurBookID = BookID; drawInit();
				 * 
				 * DrawExistingStroke(gCurBookID, gCurPageID); }
				 */

//				SetPenColor(gColor);
				/*
				 * drawSubFountainPen2(bDrawl[0], gScale, gOffsetX, gOffsetY, gWidth, pointX,
				 * pointY, pointZ, 0); // drawSubFountainPen3(bDrawl[0], gScale, gOffsetX,
				 * gOffsetY, // gWidth, pointX, pointY, pointZ);
				 * 
				 * // 保存屏幕坐标，原始坐标会使比例缩小 saveData(gCurBookID, gCurPageID, pointX, pointY, pointZ,
				 * 0, gWidth, gColor, dot.Counter, dot.angle); mov_x = pointX; mov_y = pointY;
				 */
				return;
			}

			if (dot.type == Dot.DotType.PEN_MOVE) {
				penUpTime = System.currentTimeMillis();
				SmartPenStrokeBuffer.add(new GesturePoint((float) (dot.x + dot.fx / 100.0),
						(float) (dot.y + dot.fy / 100.0), System.currentTimeMillis()));
//				temContair.add(new Position((float) (dot.x + dot.fx / 100.0), (float) (dot.y + dot.fy / 100.0)));
				updateUsingInfo("x坐标:" + dot.x + "   " + "y坐标:" + dot.y, PENPOINTINF);
//				SmartPenStrokeBuffer.add(new GesturePoint(pointX, pointY, System.currentTimeMillis()));
//				mSmartPenStrokeBuffer.add(new MGesturePoint(pointX,pointY,System.currentTimeMillis()));
				// Log.i(TAG, "PEN_MOVE");
				// gPIndex = 0;
				// Pen Move
				/*
				 * gPIndex += 1; mN += 1; // Log.e("zgm", //
				 * "pointX:"+(pointX-mov_x)+" pointY:"+(pointY-mov_y)); mov_x = pointX; mov_y =
				 * pointY;
				 */
//				SetPenColor(gColor);
				/*
				 * if (!groupstatus) {//如果没有组队就判断是不是组队操作 if(Math.abs(pointX-firstPointX
				 * )<2&&Math.abs(pointX-firstPointY)<2){
				 * 
				 * if (Math.abs(dot.timelong-firstPointTime)>=500) {
				 * showInftTextView.setText("进入组队模式:已有一直笔加入"); Log.e("zgm","进入组队模式:已有一直笔加入" );
				 * // byte[] mbyte=ObjAndByte.ObjectToByte(groupstatus); // sendMessage(mbyte);
				 * // sendMessage("aaaaa"); //打开组队蓝牙 groupstatus=true;
				 * 
				 * } } }
				 */
				// firstPointTime=dot.timelong;
				/*
				 * if (dot.Counter <= 15) { drawSubFountainPen2(bDrawl[0], gScale, gOffsetX,
				 * gOffsetY, gWidth, pointX, pointY, pointZ, 0); saveData(gCurBookID,
				 * gCurPageID, pointX, pointY, pointZ, 0, gWidth, gColor, dot.Counter,
				 * dot.angle); } else { drawSubFountainPen2(bDrawl[0], gScale, gOffsetX,
				 * gOffsetY, gWidth, pointX, pointY, pointZ, 1); bDrawl[0].invalidate(); //
				 * 保存屏幕坐标，原始坐标会使比例缩小 saveData(gCurBookID, gCurPageID, pointX, pointY, pointZ, 1,
				 * gWidth, gColor, dot.Counter, dot.angle); }
				 */

				// drawSubFountainPen3(bDrawl[0], gScale, gOffsetX, gOffsetY,
				// gWidth, pointX, pointY, pointZ);
			}
		} else if (dot.type == Dot.DotType.PEN_UP) {
			// bleManager.setPenBeepMode(false);
			// Log.i(TAG, "PEN_UP");
			// Pen Up
			// groupstatus=false;
			/*
			 * // * 画图设置
			 */
//		bDrawl[0].canvas.restore();
			/*
			 * 画图设置 完
			 * 
			 */
			if (dot.x == 0 || dot.y == 0) {
				pointX = mov_x;
				pointY = mov_y;
			}
			intPenStrokeCount++;
			updateUsingInfo(intPenStrokeCount + "", PENSTROKECOUNT);
			SmartPenStrokeBuffer.add(new GesturePoint((float) (dot.x + dot.fx / 100.0),
					(float) (dot.y + dot.fy / 100.0), System.currentTimeMillis()));
//			temContair.add(new Position((float) (dot.x + dot.fx / 100.0), (float) (dot.y + dot.fy / 100.0)));
//			SmartPenStrokeBuffer.add(new GesturePoint(pointX, pointY, System.currentTimeMillis()));
//			mSmartPenStrokeBuffer.add(new MGesturePoint(pointX,pointY,System.currentTimeMillis()));
//			dealSmartPenGesture.dealWithGesture(mCurrentGesture,mCurrentGesture.getGestureBoundBoxRect());
			gPIndex += 1;
			/*
			 * drawSubFountainPen2(bDrawl[0], gScale, gOffsetX, gOffsetY, gWidth, pointX,
			 * pointY, pointZ, 2); // drawSubFountainPen3(bDrawl[0], gScale, gOffsetX,
			 * gOffsetY, // gWidth, pointX, pointY, pointZ); // 保存屏幕坐标，原始坐标会使比例缩小
			 * saveData(gCurBookID, gCurPageID, pointX, pointY, pointZ, 2, gWidth, gColor,
			 * dot.Counter, dot.angle); bDrawl[0].invalidate();
			 */

			pointX = 0;
			pointY = 0;
			mN = 0;
			gPIndex = -1;
			transferToGesture(SmartPenStrokeBuffer);
		} // dot.type == Dot.DotType.PEN_UP代码块结束
		penUpTime = System.currentTimeMillis();
		if (firstpen == true) {
			firstpen = false;
			new Thread(new Runnable() {

				@SuppressWarnings("deprecation")
				@Override
				public void run() {
					firstpen = false;
					curTime = System.currentTimeMillis();
//Log.e("di","1129:hah计时开始："+System.currentTimeMillis());				
					while (curTime - penUpTime < 1000) {
						curTime = System.currentTimeMillis();
					}
					{
						firstpen = true;
						firstPenChi = true;
						dealSmartPenGesture.dealWithGesture(currentSmartPenGesture);
//					Log.e("di", "1129:计时完成："+System.currentTimeMillis());
					}
				}

			}).start();
		}

	}

	private void transferToGestureStroke(ArrayList<GesturePoint> smartPenStrokeBuffer2) {

	}

	/**
	 * 将ArrayList<GesturePoint>转化成安卓手势SmartPenGesture中的一笔SmartPenGesture
	 * 
	 * @param mSmartPenStrokeBuffer2
	 */
	private void transferToGesture(ArrayList<GesturePoint> mSmartPenStrokeBuffer2) {
		// TODO Auto-generated method stub
		/*
		 * if (mCurrentGesture==null) { mCurrentGesture= new MGesture(new
		 * MGestureStroke(mSmartPenStrokeBuffer2)); return; }else { if (true) {
		 * mCurrentGesture.clearMGestureStroke(); mCurrentGesture.addMGestureStroke(new
		 * MGestureStroke(mSmartPenStrokeBuffer2));
		 * 
		 * } }
		 */

		if (currentSmartPenGesture == null) {
			currentSmartPenGesture = new SmartPenGesture();
			currentSmartPenGesture.addStroke(new GestureStroke(mSmartPenStrokeBuffer2));
			firstPenChi = false;
			return;
		} else {
			if (firstPenChi) {
				firstPenChi = false;
				currentSmartPenGesture.SmartPenGestureClearAllStroke();
//				Log.e("zgm", "0108:" + currentSmartPenGesture.getBoundingBox());
				currentSmartPenGesture.SmartPenGestureClearmBoundingBox();
			}
//			Log.e("zgm", "01181:" + currentSmartPenGesture.getStrokesCount());
			currentSmartPenGesture.addStroke(new GestureStroke(mSmartPenStrokeBuffer2));
//			Log.e("zgm", "01182:" + currentSmartPenGesture.getStrokesCount());
		}

	}

	/**
	 * 将Map<String,ArrayList<SimplePoint>>转化为安卓手势
	 * 
	 * @param gestureMap
	 * @return
	 */
	public Gesture mapToGesture(Map<String, ArrayList<SimplePoint>> gestureMap) {
		Gesture transferedGesture = new Gesture();
		ArrayList<SimplePoint> mapStroke;
		ArrayList<GesturePoint> gesturePoints;
		for (int i = 1; i < gestureMap.size(); i++) {
			mapStroke = gestureMap.get(i + "");
			gesturePoints = new ArrayList<GesturePoint>();
			for (SimplePoint simplePoint : mapStroke) {
				gesturePoints.add(new GesturePoint(simplePoint.x, simplePoint.y, (long) simplePoint.timestamp));
			}
			transferedGesture.addStroke(new GestureStroke(gesturePoints));
		}
		return currentSmartPenGesture;

	}

	/*
	 * 根据传入的原始点的部分属性重新打包成Dots，并存放在bookID对应的dot_number
	 * dot_number类型为ArrayListMultimap<Integer, Dots>
	 */
	private void saveData(Integer bookID, Integer pageID, float pointX, float pointY, int force, int ntype,
			int penWidth, int color, int counter, int angle) {
		Log.i(TAG, "======savaData pageID======" + pageID + "========sdfsdf" + angle);
		Dots dot = new Dots(bookID, pageID, pointX, pointY, force, ntype, penWidth, color, counter, angle);

		try {
			if (bookID == 100) {
				dot_number.put(pageID, dot);
			} else if (bookID == 0) {
				dot_number1.put(pageID, dot);
			} else if (bookID == 1) {
				dot_number2.put(pageID, dot);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void ProcessDots(Dot dot) {
		// //Log.i(TAG, "=======222draw dot=======" + dot.toString());
		/*
		 * // 回放模式，不接受点 if (bIsReply) { return; }
		 */
		putPointIntoPage(dot);
		ProcessEachDot(dot);

	}

	private void saveOutDotLog(Integer bookID, Integer pageID, float pointX, float pointY, int force, int ntype,
			int penWidth, int color, int counter, int angle) {
		// Log.i(TAG, "======savaData pageID======" + pageID + "========sdfsdf"
		// + angle);
		Dots dot = new Dots(bookID, pageID, pointX, pointY, force, ntype, penWidth, color, counter, angle);

		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		SimpleDateFormat formatter1 = new SimpleDateFormat("yyyyMMdd");
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		String str = formatter.format(curDate);
		String str1 = formatter1.format(curDate);
		String hh = str.substring(0, 2);

		if (!gStrHH.equals(hh)) {
			// Log.i(TAG, "sssssss " + gStrHH + " " + hh);
			gStrHH = hh;
			bLogStart = true;
		}

		String txt = str + "BookID: " + bookID + " PageID: " + pageID + " Counter: " + counter + "  pointX: " + gpointX
				+ "  pointY: " + gpointY + "  force: " + force + "  angle: " + angle;
		String fileName = str1 + gStrHH + ".log";
		if (isSaveLog) {
			if (bLogStart) {
				BLEFileUtil.writeTxtToFile("-------------------------TQL SmartPen LOG--------------------------",
						LOGPATH, fileName);
				bLogStart = false;
			}

			BLEFileUtil.writeTxtToFile(txt, LOGPATH, fileName);
		}
	}

	public void DrawExistingStroke(int BookID, int PageID) {
		if (BookID == 100) {
			dot_number4 = dot_number;
		} else if (BookID == 0) {
			dot_number4 = dot_number1;
		} else if (BookID == 1) {
			dot_number4 = dot_number2;
		}

		if (dot_number4.isEmpty()) {
			return;
		}

		Set<Integer> keys = dot_number4.keySet();
		for (int key : keys) {
			// Log.i(TAG, "=========pageID=======" + PageID + "=====Key=====" +
			// key);
			if (key == PageID) {
				List<Dots> dots = dot_number4.get(key);
				for (Dots dot : dots) {
					// Log.i(TAG, "=========pageID=======" + dot.pointX + "===="
					// + dot.pointY + "===" + dot.ntype);

					drawSubFountainPen1(bDrawl[0], gScale, gOffsetX, gOffsetY, dot.penWidth, dot.pointX, dot.pointY,
							dot.force, dot.ntype, dot.ncolor);
				}
			}
		}

		bDrawl[0].postInvalidate();
		gPIndex = -1;
	}

	private void drawSubFountainPen1(DrawView DV, float scale, float offsetX, float offsetY, int penWidth, float x,
			float y, int force, int ntype, int color) {
		if (ntype == 0) {
			g_x0 = x;
			g_y0 = y;
			g_x1 = x;
			g_y1 = y;
			// Log.i(TAG, "--------draw pen down-------");
		}

		if (ntype == 2) {
			g_x1 = x;
			g_y1 = y;
			Log.i("TEST", "--------draw pen up--------");
			// return;
		} else {
			g_x1 = x;
			g_y1 = y;
			// Log.i(TAG, "--------draw pen move-------");
		}

		DV.paint.setStrokeWidth(penWidth);
//		SetPenColor(color);
		DV.canvas.drawLine(g_x0, g_y0, g_x1, g_y1, DV.paint);
		g_x0 = g_x1;
		g_y0 = g_y1;

		return;
	}

	private void drawSubFountainPen2(DrawView DV, float scale, float offsetX, float offsetY, int penWidth, float x,
			float y, int force, int ntype) {
//		Log.e("zgm", "执行函数drawSubFountainPen2");
		if (ntype == 0) {
			g_x0 = x;
			g_y0 = y;
			g_x1 = x;
			g_y1 = y;
			// Log.i(TAG, "--------draw pen down-------");
		}
		if (ntype == 2) {
			g_x1 = x;
			g_y1 = y;
			Log.i("TEST", "--------draw pen up--------");
		} else {
			g_x1 = x;
			g_y1 = y;
			// Log.i(TAG, "--------draw pen move-------");
		}

		DV.paint.setStrokeWidth(penWidth);

//		DV.paint.setColor(Color.RED);
//		DV.canvas.saveLayer(new RectF(DV.canvas.getClipBounds()), DV.paint, Canvas.ALL_SAVE_FLAG);
//		DV.canvas.save(Canvas.ALL_SAVE_FLAG);
		DV.canvas.drawLine(g_x0, g_y0, g_x1, g_y1, DV.paint);
//		DV.canvas.restore();
		DV.invalidate();
		g_x0 = g_x1;
		g_y0 = g_y1;

		return;
	}

	/*
	 * onActivityResult用来接收Intent的数据
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_SELECT_DEVICE:
			// When the DeviceListActivity return, with the selected device
			// address
			if (resultCode == Activity.RESULT_OK && data != null) {
				String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
				try {
					boolean flag = mService.connect(deviceAddress);
					penAddress = deviceAddress;
					if (penAddress.equals("00:12:6F:5B:14:08")) {
						studentNumber = "0944";
						updateUsingInfo("学号:" + studentNumber, USERNAME);
					}
					if (penAddress.equals("B0:F1:EC:BD:F2:3F")) {
						studentNumber = "0945";
						updateUsingInfo("学号:" + studentNumber, USERNAME);
					}
					if (penAddress.equals("00:12:6F:5B:14:1B")) {
						studentNumber = "0946";
						updateUsingInfo("学号:" + studentNumber, USERNAME);
					}
					if (penAddress.equals("00:12:6F:5B:14:05")) {
						studentNumber = "0947";
						updateUsingInfo("学号:" + studentNumber, USERNAME);
					}
					if (penAddress.equals("B0:F1:EC:BD:CD:10")) {
						studentNumber = "0948";
						updateUsingInfo("学号:" + studentNumber, USERNAME);
					}
					if (true) {
						showSound(R.raw.smartconnected);
						new Thread(new Runnable() {
							@Override
							public void run() {
								while (true) {

									try {
										bleManager.getPenAllStatus();
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									penName = ApplicationResources.mPenName + "";
									penAdress = penAddress;
									penPower = ApplicationResources.mBattery + "%";
									updateUsingInfo("", 100);// 上面已经更新过信息字符，不需再次更新，只是重新显示即可，100是规定的信息类型的随便的一个数
									try {
										Thread.sleep(180 * 1000);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}

								}
							}
						}).start();
					}
					// TODO spp
					// bleManager.setSppConnect(deviceAddress);
				} catch (Exception e) {
					// Log.i(TAG, "connect-----" + e.toString());
				}
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();
			} else {
				// User did not enable Bluetooth or an error occurred
				Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
				finish();
			}
			break;
		case REQUEST_CONNECT_DEVICE_SECURE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {// 结果码
				connectDevice(data, true);
			}
			break;
		case GET_FILEPATH_SUCCESS_CODE:
			if (resultCode == Activity.RESULT_OK) {
				String path = "";
				Uri uri = data.getData();
				/*
				 * if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { String pathFromURI =
				 * null; try { pathFromURI = getRealPathFromURI(uri); } catch (Exception e) {
				 * e.printStackTrace(); } String[] split = pathFromURI.split("/"); path =
				 * split[split.length - 1]; } else if (Build.VERSION.SDK_INT ==
				 * Build.VERSION_CODES.LOLLIPOP_MR1) { String uriPath = uri.getPath(); String[]
				 * split = uriPath.split("/"); path = split[split.length - 1]; }
				 */
				final String str = path;
				// Log.i(TAG, "onActivityResult: path="+str);
				new Thread(new Runnable() {
					@Override
					public void run() {
						bleManager.readTestData(str);
					}
				}).start();
			}
			break;
		default:
			Log.e(TAG, "wrong request code");
			break;
		}
	}

	private void SetBackgroundImage(int BookID, int PageID) {
		/*
		 * if (!gbSetNormal) { LayoutParams para; para = gImageView.getLayoutParams();
		 * para.width = BG_WIDTH; para.height = BG_HEIGHT;
		 * gImageView.setLayoutParams(para); gbSetNormal = true;
		 * 
		 * //Log.i(TAG, "testOffset BG_WIDTH = " + BG_WIDTH + ", BG_HEIGHT =" +
		 * BG_HEIGHT + ", gcontentLeft = " + gcontentLeft + ", gcontentTop = " +
		 * gcontentTop); //Log.i(TAG, "testOffset A5_X_OFFSET = " + A5_X_OFFSET +
		 * ", A5_Y_OFFSET = " + A5_Y_OFFSET); //Log.i(TAG, "testOffset mWidth = " +
		 * mWidth + ", mHeight = " + mHeight); //Log.i(TAG, "testOffset getTop = " +
		 * gImageView.getTop() + ", getLeft = " + gImageView.getLeft()); //Log.i(TAG,
		 * "testOffset getWidth = " + gImageView.getWidth() + ", getHeight = " +
		 * gImageView.getHeight()); //Log.i(TAG, "testOffset getMeasuredWidth = " +
		 * gImageView.getMeasuredWidth() + ", getMeasuredHeight = " +
		 * gImageView.getMeasuredHeight()); }
		 * 
		 * gbCover = true; bDrawl[0].canvas.drawColor(Color.TRANSPARENT,
		 * PorterDuff.Mode.CLEAR); if (BookID == 168) { if
		 * (getResources().getIdentifier("p" + PageID, "drawable", getPackageName()) ==
		 * 0) { return; } gImageView.setImageResource(getResources().getIdentifier("p" +
		 * PageID, "drawable", getPackageName())); } else if (BookID == 100) { if
		 * (getResources().getIdentifier("p" + PageID, "drawable", getPackageName()) ==
		 * 0) { return; } gImageView.setImageResource(getResources().getIdentifier("p" +
		 * PageID, "drawable", getPackageName())); } else if (BookID == 0) { if
		 * (getResources().getIdentifier("blank" + PageID, "drawable", getPackageName())
		 * == 0) { return; }
		 * gImageView.setImageResource(getResources().getIdentifier("blank" + PageID,
		 * "drawable", getPackageName())); } else if (BookID == 1) { if
		 * (getResources().getIdentifier("zhen" + PageID, "drawable", getPackageName())
		 * == 0) { return; }
		 * gImageView.setImageResource(getResources().getIdentifier("zhen" + PageID,
		 * "drawable", getPackageName())); }
		 */
	}

	public void RunReplay() {
		if (gCurPageID < 0) {
			bIsReply = false;
			return;
		}

		drawInit();
		bDrawl[0].canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		new Thread(new Runnable() {
			@Override
			public void run() {
				ReplayCurrentPage(gCurBookID, gCurPageID, gSpeed);
			}
		}).start();
	}

	public void ReplayCurrentPage(int BookID, int PageID, int SpeedID) {
		if (BookID == 100) {
			dot_number4 = dot_number;
		} else if (BookID == 0) {
			dot_number4 = dot_number1;
		} else if (BookID == 1) {
			dot_number4 = dot_number2;
		}

		if (dot_number4.isEmpty()) {
			bIsReply = false;
			return;
		}

		Set<Integer> keys = dot_number4.keySet();
		for (int key : keys) {
			// Log.i(TAG, "=========pageID=======" + PageID + "=====Key=====" +
			// key);
			bIsReply = true;
			if (key == PageID) {
				List<Dots> dots = dot_number4.get(key);
				for (Dots dot : dots) {
					// Log.i(TAG, "=========pageID1111=======" + dot.pointX +
					// "====" + dot.pointY + "===" + dot.ntype);
					drawSubFountainPen1(bDrawl[0], gScale, gOffsetX, gOffsetY, dot.penWidth, dot.pointX, dot.pointY,
							dot.force, dot.ntype, dot.ncolor);
					// drawSubFountainPen3(bDrawl[0], gScale, gOffsetX,
					// gOffsetY, dot.penWidth, dot.pointX, dot.pointY,
					// dot.force);

					bDrawl[0].postInvalidate();
					SystemClock.sleep(SpeedID);
				}
			}
		}

		bIsReply = false;

		gPIndex = -1;
		return;
	}

	// 确保设备能够被发现
	private void ensureDiscoverable() {

		Log.d(TAG, "ensure discoverable");
		if (tableBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	private void connectDevice(Intent data, boolean secure) {
		// Get the device MAC address
		String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
		// Get the BluetoothDevice object
		BluetoothDevice device = tableBluetoothAdapter.getRemoteDevice(address);
		// Attempt to connect to the device
		mChatService.connect(device, secure);
		// String message= "had";
		// sendMessage(message);
	}

	private void setupChat() {
		Log.d(TAG, "setupChat()");

		// Initialize the array adapter for the conversation thread
		// mConversationArrayAdapter = new ArrayAdapter<String>(this,
		// R.layout.message);
		// mConversationView = (ListView) findViewById(R.id.in);
		// mConversationView.setAdapter(mConversationArrayAdapter);

		// Initialize the compose field with a listener for the return key
		// mOutEditText = (EditText) findViewById(R.id.edit_text_out);
		// mOutEditText.setOnEditorActionListener(mWriteListener);

		// Initialize the send button with a listener that for click events
		// mSendButton = (Button) findViewById(R.id.button_send);

		// Initialize the BluetoothChatService to perform bluetooth connections
		mChatService = new BluetoothChatService(this, mHandler);

		// Initialize the buffer for outgoing messages
		mOutStringBuffer = new StringBuffer("");
	}

	/**
	 * Sends a message.
	 * 
	 * @param message A string of text to send.
	 */

	private void sendMessage(String message) {
		Log.e("zgm", "Dot信息,:" + message);
		// Check that we're actually connected before trying anything
		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
			return;
		}

		// Check that there's actually something to send
		if (message.length() > 0) {
			// Get the message bytes and tell the BluetoothChatService to write
			byte[] send = message.getBytes();
			mChatService.write(send);

			// Reset out string buffer to zero and clear the edit text field
			mOutStringBuffer.setLength(0);
			// mOutEditText.setText(mOutStringBuffer);
		}
	}

	private void mSendMessage(byte[] send) {
		// Check that we're actually connected before trying anything
		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
			// Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
			// .show();
			return;
		}

		// Get the message bytes and tell the BluetoothChatService to write

		mChatService.write(send);

		// Reset out string buffer to zero and clear the edit text field
		mOutStringBuffer.setLength(0);
		// mOutEditText.setText(mOutStringBuffer);
	}

	/*
	 * 非低功耗蓝牙传过来的数据进行处理
	 */
	public void dealBlueToothMessage(byte[] readBuf) {
		if (ObjAndByte.ByteToObject(readBuf).getClass().getName().equalsIgnoreCase("com.tqltech.tqlpencomm.Dot")) {
			Log.e("zgm", "传过来的是Dot");
			final Dot mdot = (Dot) ObjAndByte.ByteToObject(readBuf);
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Log.i("zgm", "Dot信息,BookID" + mdot.BookID);
					Log.i("zgm", "Dot信息,ab_x" + mdot.ab_x);
					if (groupstatus == 5)// 如果组队成功，那么将数据发送
					{
						// byte[] dotByte=ObjAndByte.ObjectToByte(dot);
						// sendMessage(dotByte);

					}
					ProcessDots(mdot);
				}
			});
		}
		if (ObjAndByte.ByteToObject(readBuf).getClass().getName() == "boolean") {
			// =="boolean"不一定正确
		}

	}

	public void updateUsingInfo(final String str, final int infoType) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				switch (infoType) {
				case PENPOWER:
					penPower = str;
					break;
				case PENNAME:
					penName = str;
					break;
				case PENADRESS:
					penAdress = str;
					break;
				case PENPOINTINF:
					penPointInfString = str;
					break;
				case ORDERSTATE:
					orderState = str;
					break;
				case PENSTROKECOUNT:
					penStrokeCount = str;
					break;
				case USERNAME:
					mNameString = str;
					break;
				case GESTURECOUNTER:
					GestureCounter = str;
					break;
				default:
					break;
				}
				usinginf = "<b>使&nbsp;&nbsp;&nbsp;&nbsp;用&nbsp;&nbsp;&nbsp;&nbsp;者 :</b>  " + mNameString + "<br/>"
						+ "<b>智能笔名称:</b>  " + penName + "<br/>" + "<b>智能笔地址:</b>  " + penAdress + "<br/>"
						+ "<b>智能笔电量:</b>  " + penPower + "<br/>" + "<b>动 作 状 态:</b>  " + orderState + "<br/>"
						+ "<b>笔 迹 信 息:</b>  " + penPointInfString + "<br/>" + "<b>书写笔迹数:</b>" + penStrokeCount+"<br/>"
						+ "<b>书写指令数:</b>" + GestureCounter;
				showInftTextView.setText(Html.fromHtml(usinginf));
			}

		});

	}

	public void runOnUIThread(final String title, final String Message) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				builder.setTitle(title);
				builder.setMessage(Message);
				if (!builder.isShowing()) {
					builder.show();
				}
				// TODO Auto-generated method stub
//				builder.show();
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						firstOccur = true;
					}
				}, 1500);
				/*
				 * new Handler().postDelayed(new Runnable() {
				 * 
				 * @Override public void run() { builder.dismiss(); } }, 2000);
				 */
			}

		});

	}

	public void dismissAlertDialog(final AlertDialog alertDialog) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						alertDialog.dismiss();
					}
				}, 3000);
			}
		});

	}

	/*
	 * 通过readOIDSizeAndStructGroup(final int OIDSize)建立群组：
	 * （1）未建立群组前，如果智能笔按压时间短，则认为是点读功能 （2）如果按压时间达到阈值，等待另一方进行确认，此时没有点读功能（能接受到点读码）
	 * （3）当另一方确认建组后，那么群组建立完成，此时恢复点读功能
	 */

	private void groupInit() {
		groupstatus = 0;// 组对状态
		groupRequesting = false;
		groupedNumber = 0;// 本设备点击的组对码
		otherGroupedNumber = 0;// 其他设备发过来的组对码
		otherPenHaveDown = false;
		groupLeader = 1;// 因为可能发起组对请求，因此默认为组长
		otherPenUpOrDown = PENUP;
		penUpOrDown = PENUP;
		sendedMessage = false;
	}

	/**
	 * 语音提示
	 * 
	 * @param raw
	 */
	public void showSound(int raw) {
		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
			mediaPlayer.stop();
		}
		Context aContext = getApplicationContext();
		mediaPlayer = MediaPlayer.create(getApplicationContext(), raw);
		mediaPlayer.setVolume(1.0f, 1.0f);
		mediaPlayer.start();

	}

	// wsk 2019.5.7
	public void showSound(String path) {
		if (RadioIsPlaying == false) {
			if (mediaPlayer != null && mediaPlayer.isPlaying()) {
				mediaPlayer.stop();
			}
			mediaPlayer = new MediaPlayer();
			try {
				mediaPlayer.setDataSource(path);
				mediaPlayer.prepare();

			} catch (IOException e) {
				Log.e("zgm", "prepare() failed");
			}
			mediaPlayer.start();
		}
	}

	// wsk 2019.5.7
	public void ReadTiMu(ArrayList<Integer> tag) {
		if (tag == null) {
			return;
		}
		RadioIsPlaying = false;
		String path = "/storage/emulated/0/yinpin/";
		showSound(path + "shenti.mp3");
		soundTime = System.currentTimeMillis();
		curTime = System.currentTimeMillis();
		while (curTime - soundTime < 1000) {
			curTime = System.currentTimeMillis();
		}

		if (tag.get(2) == -1) {
			showSound(path + "agroup" + gCurBookID + "" + gCurPageID % 20 + ".mp3");
		}

		else {
			showSound(path + "bgroup" + gCurBookID + "" + gCurPageID % 20 + ".mp3");
		}

		soundTime = System.currentTimeMillis();
		curTime = System.currentTimeMillis();
		while (curTime - soundTime < 7000) {
			curTime = System.currentTimeMillis();
		}

		showSound(path + tag.get(0) + ".mp3");

		soundTime = System.currentTimeMillis();
		curTime = System.currentTimeMillis();
		while (curTime - soundTime < 1500) {
			curTime = System.currentTimeMillis();
		}

		showSound(path + gCurBookID + "" + (gCurPageID % 20) + "tigan" + tag.get(0) + ".mp3");
	}

	// wsk 2019.5.7
	public void readSanWeiYuYi(ArrayList<Integer> tag) {
		if (tag == null) {
			Log.e("zgm", "0415:tag为空");
			return;
		}
		if (tag.get(0) == 0) {
			Log.e("zgm", "0415:双击页眉2");
			return;
		}
		RadioIsPlaying = false;
		String path = "/storage/emulated/0/yinpin/";
		showSound(path + "sanweiyuyi" + tag.get(0) + ".mp3");
	}

	public void showVibrator() {
		Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		vibrator.vibrate(1500);
	}

	public void sendMessageManyTimes(final byte[] mbyte) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				for (int i = 0; i < 10; i++) {
					mSendMessage(mbyte);
				}
			}
		}).start();
	}

	public void saveGesture(final MGesture mgesture) {
		if (mgesture == null) {
			return;
		}
		View saveViewDialog = getLayoutInflater().inflate(R.layout.show_gesture, null);
		ImageView imageView = (ImageView) saveViewDialog.findViewById(R.id.show);
		final EditText gestureNam = (EditText) saveViewDialog.findViewById(R.id.name);
		Bitmap bitmap = mgesture.toBitmap(128, 128, 3, 0xffff0000);
		Canvas canvas = new Canvas();
//		canvas.translate(inset, inset);
		imageView.setImageBitmap(bitmap);
		new AlertDialog.Builder(MainActivity.this).setView(saveViewDialog)
				.setPositiveButton("保存", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						try {
							mGestureUnitils.addMGesture(mgesture, gestureNam.getText().toString());
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ClassNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						mGestureUnitils.save();
						Log.e("zgm", "1210:gestureNam.getText().toString:" + gestureNam.getText().toString());

						Log.e("zgm", "1210:");

					}

				}).setNegativeButton("取消", null).show();

	}

	public void saveGesture(final SmartPenGesture mgesture) {
		if (mgesture == null) {
			return;
		}
		View saveViewDialog = getLayoutInflater().inflate(R.layout.show_gesture, null);
		ImageView imageView = (ImageView) saveViewDialog.findViewById(R.id.show);
		final EditText gestureNam = (EditText) saveViewDialog.findViewById(R.id.name);
		Bitmap bitmap = mgesture.toBitmap(128, 128, 10, 0xffff0000);
		imageView.setImageBitmap(bitmap);
//		final EditText gestureNam = (EditText) saveViewDialog.findViewById(R.id.name);
		/*
		 * View saveViewDialog = getLayoutInflater().inflate(R.layout.gestureinfor,
		 * null); ImageView imageView = (ImageView)
		 * saveViewDialog.findViewById(R.id.show); Bitmap bitmap =
		 * mgesture.toBitmap(128, 128, 10, 0xffff0000);
		 * imageView.setImageBitmap(bitmap); TextView gestureOwner=(TextView)
		 * saveViewDialog.findViewById(R.id.gesture_owner);
		 * gestureOwner.setText("使 用者："+mNameString); TextView
		 * gesturePosition=(TextView)
		 * saveViewDialog.findViewById(R.id.gesture_position); EditText gestureYuyi=
		 * (EditText) saveViewDialog.findViewById(R.id.gesture_yuyi); TextView
		 * gestureName=(TextView) saveViewDialog.findViewById(R.id.gesture_name);
		 * TextView gestureResponce=(TextView)
		 * saveViewDialog.findViewById(R.id.gesture_responce); if
		 * (dealSmartPenGesture!=null) { if (dealSmartPenGesture.tag!=null) {
		 * gesturePosition.setText("位  置：第"+dealSmartPenGesture.tag.get(0)+"题题干区"); }
		 * gestureName.setText("手势名称："+dealSmartPenGesture.gestureFinalName);
		 * gestureResponce.setText("响应方式："+dealSmartPenGesture.gestureResponce); }
		 */

		new AlertDialog.Builder(MainActivity.this).setView(saveViewDialog)
				.setPositiveButton("保存", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
//                             GestureLibrary gestureLibrary=GestureLibraries.fromFile("/sdcard/zgmgesture");

						gestureLibrary.addGesture(gestureNam.getText().toString(), mgesture);
//							Log.e("zgm", "1210:gestureNam.getText().toString:"+gestureNam.getText().toString());
						gestureLibrary.save();
						gestureLibrary = GestureLibraries.fromFile("/sdcard/zgmgesture");
//						Log.e("zgm", "1210:"+gestureLibrary.save());

					}

				}).setNegativeButton("取消", null).show();

	}

	public void recognizeGesture(final MGesture mCurrentGesture2) {
		// 装载手势文件：
		ArrayList<MGesture> mGesturesContainer = null;
		try {
			mGesturesContainer = mGestureUnitils.load();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (mGesturesContainer != null) {
			Log.e("zgm", "1210：手势文件装载成功");
			MGesture predictionMGesture = mGestureUnitils.recogniseGeMGesture(mCurrentGesture2);
			final AlertDialog builder = new AlertDialog.Builder(MainActivity.this).create();
			if (predictionMGesture != null) {
				String resultsString = predictionMGesture.getGestureName();
				builder.setMessage(resultsString);
				builder.show();
			} else {
				builder.setMessage("无法找到匹配的手势");
			}
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					builder.dismiss();
				}
			}, 2000);
		} else {
			Log.e("zgm", "1210：手势文件装载失败");
		}
	}

	public void recognizeGesture(final SmartPenGesture mCurrentGesture2) {
		// 装载手势文件：
		String gestureName = null;
		if (gestureLibrary.load()) {
			Log.e("zgm", "1210：手势文件装载成功");
			ArrayList<Prediction> predictions = gestureLibrary.recognize(mCurrentGesture2);
			ArrayList<String> gestureNames = new ArrayList<String>();
			for (Prediction prediction : predictions) {
				if (prediction.score > 2.5) {
					gestureNames.add(prediction.name);
				}
			}
			if (gestureNames.size() > 0) {
				Log.e("zgm", "1210：手势文件装载成功");
				final AlertDialog builder = new AlertDialog.Builder(MainActivity.this).create();

				builder.setMessage(gestureNames.get(0));
				builder.show();
			} else {
				builder.setMessage("无法找到匹配的手势");
			}
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					builder.dismiss();
				}
			}, 2000);
		}

		else {
			Log.e("zgm", "1210：手势文件装载失败");
		}
	}

	public RectF getGestureRectF() {

		return null;

	}

	public void showRecordDialog(boolean control) {
		AlertDialog recordBuilder = new AlertDialog.Builder(MainActivity.this).create();
		View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.fragment_record_audio, null);
		recordBuilder.setView(view);
	}

	public void putPointIntoPage(Dot dot) {
		if (smartPenPage == null) {
			String fileName = "NONE-" + studentNumber + "-" + dot.BookID + "-" + dot.PageID + "-0.page";

			if (smartPenPageNameContainer.contains(fileName)) {
				int index = smartPenPageNameContainer.indexOf(fileName);
				smartPenPage = smartPenPageContainer.get(index);
			} else {
				Log.e("0507", "fileName4:" + fileName);
//			reference.compareAndSet(smartPenPage,new SmartPenPage(dot.PageID, dot.BookID, mgroupedNumber));
				smartPenPage = new SmartPenPage(dot.PageID, dot.BookID, mgroupedNumber);
				smartPenPageContainer.add(smartPenPage);
				smartPenPageNameContainer.add(fileName);
			}
		}
		if (dot.PageID != smartPenPage.pageNumber || dot.BookID != smartPenPage.bookeId) {
//			SmartPenUnitils.save(smartPenPage);
			String fileName = "NONE-" + studentNumber + "-" + dot.BookID + "-" + dot.PageID + "-0.page";
			Log.e("0507", "fileName1:" + fileName);
			if (smartPenPageNameContainer.contains(fileName)) {
				int index = smartPenPageNameContainer.indexOf(fileName);
				smartPenPage = smartPenPageContainer.get(index);
			} else {
				Log.e("0507", "fileName4:" + fileName);
//					reference.compareAndSet(smartPenPage,new SmartPenPage(dot.PageID, dot.BookID, mgroupedNumber));
				smartPenPage = new SmartPenPage(dot.PageID, dot.BookID, mgroupedNumber);
				smartPenPageContainer.add(smartPenPage);
				smartPenPageNameContainer.add(fileName);
			}
		}
		smartPenPage.addStrokePoint(dot);
	}

	public void drawsmartpenpoints(final SmartPenPage sPenPage) {
		if (sPenPage == null) {
			runOnUIThread("警告", "文件不存在或者不可读");
			dismissAlertDialog(builder);
			return;
		}
//		SetPenColor(1);
//		String fileName=sPenPage.getPageFileName();
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				for (int i = 0; i < sPenPage.getAllPoints().size(); i++) {
					for (Dot dot : sPenPage.getAllPoints().get((long) (i + 1))) {
						ProcessEachDot(dot);
					}
				}
//				SetPenColor(gColor);
			}

		});
	}

	public SmartPenPage getfromFile(String pathString, String fileName) {
		SmartPenPage aPage = null;
		try {
//			aPage = SmartPenUnitils.load("sdcard/-1/zgm10_1_0.page");
			aPage = SmartPenUnitils.load(pathString);
			aPage.setPageFileName(fileName);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return aPage;
	}

	public void changetoslef(View v) {
		switch (v.getId()) {
		case R.id.group_people1:
			if (groupLeader == 1 && correcting) {
				runOnUIThread("通知", "退出了批改状态");
				dismissAlertDialog(builder);

				correcting = false;
				showSound(R.raw.in);
				showVibrator();// 震动
			}
			break;
		case R.id.group_people2:
			if (groupLeader == 0 && correcting) {
				runOnUIThread("通知", "退出了批改状态");
				dismissAlertDialog(builder);
				correcting = false;
				showSound(R.raw.in);
				showVibrator();// 震动
			}
			break;
		case R.id.group_people3:

			break;
		case R.id.group_people4:

			break;

		default:
			break;
		}

	}

	public void setmode(View v) {
		switch (v.getId()) {
		case R.id.close:
			/*
			 * groupinfo.setVisibility(View.GONE); gestureinfo.setVisibility(View.GONE);
			 * showInftTextView.setVisibility(View.GONE);
			 */
			informationLayout.setVisibility(View.INVISIBLE);
			pen_chirography_all.setVisibility(View.INVISIBLE);
			break;
		case R.id.unfold:
			informationLayout.setVisibility(View.VISIBLE);
			pen_chirography_all.setVisibility(View.VISIBLE);

//			informationLayout.setVisibility(View.VISIBLE);	
			/*
			 * if (smartPenPage!=null) {
			 * mSendMessage(ObjAndByte.ObjectToByte(smartPenPage)); } else { return; }
			 */
			break;
		case R.id.watch:
			informationLayout.setVisibility(View.INVISIBLE);
			pen_chirography_all.setVisibility(View.INVISIBLE);
			pencage.setVisibility(View.INVISIBLE);
//			initMiniShowAreaVisibility();
			minishowlayout.setVisibility(View.VISIBLE);
			setMiniShowViewShow(witchShow);
//			informationLayout.setVisibility(View.VISIBLE);	
			/*
			 * if (smartPenPage!=null) {
			 * mSendMessage(ObjAndByte.ObjectToByte(smartPenPage)); } else { return; }
			 */
			break;
		case R.id.submit:
			if (smartPenPage != null) {
				SmartPenUnitils.save(smartPenPage);

//				mSendMessage(ObjAndByte.ObjectToByte(smartPenPage.getAllPoints()));

			} else {
				return;
			}
			break;
		case R.id.do_work:
			showSound(R.raw.do_work);
//			penChirography.setBackgroundResource(R.drawable.pagebg);
			break;
		case R.id.correcting:
			showSound(R.raw.correcting);
//			penChirography.setBackgroundResource(R.drawable.done);
			break;
		case R.id.revision:
			showSound(R.raw.revision);
//			penChirography.setBackgroundResource(R.drawable.corrected);
			break;
		case R.id.refresh:
			if (true) {
				Button doWorkButton = (Button) findViewById(R.id.do_work);
				Button correcting = (Button) findViewById(R.id.correcting);
				Button revision = (Button) findViewById(R.id.revision);
				count++;
//				Log.e("zgm", "0126"+(count%4));
				switch (count % 4) {

				case 0:
					doWorkButton.setVisibility(View.INVISIBLE);
					correcting.setVisibility(View.INVISIBLE);
					revision.setVisibility(View.INVISIBLE);
					return;
				case 1:

					doWorkButton.setVisibility(View.VISIBLE);
					correcting.setVisibility(View.INVISIBLE);
					revision.setVisibility(View.INVISIBLE);
					return;
				case 2:
					doWorkButton.setVisibility(View.INVISIBLE);
					correcting.setVisibility(View.VISIBLE);
					showDownLoadDialog();
					Toast.makeText(getBaseContext(), "下载成功", Toast.LENGTH_SHORT);
					revision.setVisibility(View.INVISIBLE);
					return;
				case 3:
					doWorkButton.setVisibility(View.INVISIBLE);
					correcting.setVisibility(View.INVISIBLE);
					revision.setVisibility(View.VISIBLE);
					return;

				default:
					break;
				}
			}
			break;
		case R.id.maintextview:
			showSound(R.raw.in);
			doDownLoadWork();
			break;
		case R.id.updatehomeworkinfo:
			showSound(R.raw.in);
			doDownLoadWork();
			break;
		case R.id.scanforsmartpen:
			serverIntent = new Intent(this, SelectDeviceActivity.class);
			startActivityForResult(serverIntent, REQUEST_SELECT_DEVICE);
			break;
		case R.id.changeview:
			showInfoViewCouter++;
			switch (showInfoViewCouter % 3) {

			case 0:
				smartPenInforLinearLayout.setVisibility(View.VISIBLE);
				groupInforLinearLayout.setVisibility(View.GONE);
				gestureInforLinearLayout.setVisibility(View.GONE);
				break;
			case 1:
				smartPenInforLinearLayout.setVisibility(View.GONE);
				groupInforLinearLayout.setVisibility(View.VISIBLE);
				gestureInforLinearLayout.setVisibility(View.GONE);
				break;
			case 2:
				smartPenInforLinearLayout.setVisibility(View.GONE);
				groupInforLinearLayout.setVisibility(View.GONE);
				gestureInforLinearLayout.setVisibility(View.VISIBLE);
				break;
			default:
				break;
			}
			break;
		case R.id.dynamic_debug_body_title:
			showSound(R.raw.in);
			doDownLoadWork();
			break;
		case R.id.end_ask:
//			dealSmartPenGesture.recordAndio(null);// 低功耗蓝牙(智能笔)初始化
//			showDownLoadDialog();
			showSound(R.raw.in);
//			Toast.makeText(getBaseContext(), "请订正", Toast.LENGTH_SHORT);
			homeworkStatus = 3;
			doDownLoadWork();
//			doDownLoadWork("123-1.page");

			Toast.makeText(getBaseContext(), "请订正", Toast.LENGTH_SHORT);
			homeworkStatus = 3;
			break;
		case R.id.leftarrow:
			if (witchShow > 0) {
				witchShow--;
				setMiniShowViewShow(witchShow);
			}
			break;
		case R.id.rightarrow:
			if (witchShow < 4) {
				witchShow++;
				setMiniShowViewShow(witchShow);
			}
			break;

		default:

			break;
		}
	}

	public void mProcessDots(final Dot dot) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				ProcessEachDot(dot);
			}
		});

	}

	public void joinOrCreateGroup(final int OIDSize) {
		lastOidNumber = OIDSize;
		lastOidTime = System.currentTimeMillis();
		Log.e("zgm", "1213：lastOidTime=" + System.currentTimeMillis());
		Log.e("zgm", "1213：penUpOrDown=" + penUpOrDown);
		if (penUpOrDown == PENUP) {
			penUpOrDown = PENDOWN;//
			new Thread(new Runnable() {

				@Override
				public void run() {
					Looper.prepare();
					// TODO Auto-generated method stub
					while (System.currentTimeMillis() - lastOidTime < 500) {// 如果该线程的父线程中处理代码速度慢，该值需要调大一点，才能正确识别是否真正抬笔
						// 空循环
//						Log.e("zgm", "120111:"+ lastpointTime);
					}
					Log.e("zgm", "1213：penUpOrDown抬笔1=" + penUpOrDown);
					penUpOrDown = PENUP;// 笔抬起来了
					firstOccur = true;
					if (groupstatus == 0 || groupstatus == 8) {
						// 点读操作
						groupstatus = 0;
						groupLeader = 1;
						updateUsingInfo("点读操作，点读码是：" + lastOidNumber, ORDERSTATE);
						return;
					}
					if (groupstatus == 5) {// 组对成功
						groupstatus = 9;
						return;
					}
					if (groupstatus == 9) {
						updateUsingInfo("小组状态！！！点读操作，点读码是：" + lastOidNumber, ORDERSTATE);
						return;
					}
					if (groupstatus == 6) {
						updateUsingInfo("正确的组对码是：" + otherGroupedNumber + "，请点击正确的组对码！！！！", ORDERSTATE);
						return;
					}
					{// 组对过程中,组对未完成
						groupstatus = 0;
						groupLeader = 1;
						updateUsingInfo("组对中止！！！！", ORDERSTATE);
						Log.e("zgm", "1213：penUpOrDown抬笔2=" + penUpOrDown);
						Log.e("zgm", "1213：groupstatus=" + groupstatus);

						if (firstOccur) {
							firstOccur = false;
							showSound(R.raw.in);
							showVibrator();// 震动

							runOnUIThread("组对状态通知！！！", "提前抬笔，组对中止！！！");
							dismissAlertDialog(builder);

						}
						StringMessage mStringMessage = new StringMessage(GROUP_CANCLE, "" + OIDSize, mNameString);
						byte[] mbyte = ObjAndByte.ObjectToByte(mStringMessage);
						sendMessageManyTimes(mbyte);
						firstOccur = true;// 抬笔必须保证firstOccur=true
						return;
					}

				}

			}).start();
			firstTime = System.currentTimeMillis();
		}
		switch (groupstatus) {
		case 0:// 未组对状态
			groupLeader = 1;
			Log.e("zgm", "1213：groupstatus=" + groupstatus);

			if (System.currentTimeMillis() - firstTime > 3000) {
				Log.e("zgm", "1213：groupstatus=" + groupstatus + " System.currentTimeMillis()-firstTime="
						+ (System.currentTimeMillis() - firstTime));
				// 发送组对请求
				StringMessage mStringMessage = new StringMessage(GROUP_REQUEST, "" + OIDSize, mNameString);
				byte[] mbyte = ObjAndByte.ObjectToByte(mStringMessage);
				sendMessageManyTimes(mbyte);
				groupstatus = 1;// 状态改变
				if (firstOccur) {
					firstOccur = false;
					updateUsingInfo("你进入组对状态，组对码是：" + groupedNumber, ORDERSTATE);
					showSound(R.raw.in);
					showVibrator();// 震动
					Log.e("zgm", "1213：firstOccur=" + firstOccur);
					runOnUIThread("建组状态通知!", "你进入组对状态，组对码是：" + groupedNumber);
//					builder.setTitle("建组状态通知!");
//					builder.setMessage("你进入组对状态，组对码是："+groupedNumber);
					builder.show();
//					dismissAlertDialog(builder);

					/*
					 * new Handler().postDelayed(new Runnable() {
					 * 
					 * @Override public void run() { firstOccur=true; } }, 500);
					 */

				}
			}

			break;
		case 1:// 发送完组队通知等待答复
			Log.e("zgm", "1213：groupstatus=" + groupstatus);
			// 等待不做任何处理
			break;
		case 2:// 发送完组对通知后得到接受组对答复(同意组对)
			Log.e("zgm", "1213：groupstatus=" + groupstatus);
			if (firstTOBeStatus) {
				firstTOBeStatus = false;
				firstTime = System.currentTimeMillis();
			} else {
				updateUsingInfo("进入组对状态，组对码是：" + groupedNumber + ",正在计时中", ORDERSTATE);
				if (System.currentTimeMillis() - firstTime > 3000) {// 组对成功
					// 发送组对成功通知，改变组对状态
					mStringMessage = new StringMessage(GROUP_SUCESS, "" + OIDSize, mNameString);
					mbyte = ObjAndByte.ObjectToByte(mStringMessage);
					sendMessageManyTimes(mbyte);
					groupstatus = 4;
					firstTOBeStatus = true;
				}
			}

			break;
		case 3:// 发送完组对通知后得到拒绝组对的答复
			Log.e("zgm", "1213：groupstatus=" + groupstatus);
			// 暂时用不到，因为另外一方不回发送拒绝请求
			break;
		case 4:// 发送完组对成功等待答复
			Log.e("zgm", "1213：groupstatus=" + groupstatus);
			// 等待不做处理
			break;
		case 5:// 发送完组对成功后收到回复(组对完成)
			Log.e("zgm", "1213：groupstatus=" + groupstatus);
			if (groupLeader == 0) {
				updateUsingInfo("C", USERNAME);
				initGroupPeople();
				groupPeople2.setBackgroundColor(Color.GREEN);
				/*
				 * runOnUIThread("组对状态通知!",
				 * mNameString+"和"+otherNameString+"组队成功！！！！您的成员类别是:组员");
				 * updateUsingInfo(mNameString+"和"+otherNameString+"组队成功！！！！您的成员类别是:组员",
				 * ORDERSTATE);
				 */
				/*
				 * updateUsingInfo(mNameString+"和"+otherNameString+"组队成功！！！！您的成员类别是:组员",
				 * ORDERSTATE);
				 */
				;
			} else {
				updateUsingInfo("A", USERNAME);
				initGroupPeople();
				groupPeople1.setBackgroundColor(Color.GREEN);
				/* updateUsingInfo(mNameString+"和"+"C"+"组队成功！！！！您的成员类别是:组长",ORDERSTATE); */
				/* updateUsingInfo(mNameString+"和"+"C"+"组队成功！！！！您的成员类别是:组长",ORDERSTATE); */
			}
			dismissAlertDialog(builder);
			break;
		case 6:// 收到组对通知后未答复
			Log.e("zgm", "1213：groupstatus=" + groupstatus);
			groupedNumber = OIDSize;
			if (groupedNumber == otherGroupedNumber) {
				groupstatus = 7;// 状态改变
				updateUsingInfo("进入组对状态，组对码是：" + groupedNumber + ",正在计时中", ORDERSTATE);
				showSound(R.raw.in);
				showVibrator();// 震动
				StringMessage mStringMessage = new StringMessage(GROUP_ALREADY, OIDSize + "", mNameString);
				byte[] mbyte = ObjAndByte.ObjectToByte(mStringMessage);
				sendMessageManyTimes(mbyte);

			} else {

				if (firstOccur) {
					updateUsingInfo("组对码错误，正确的组对码是：" + otherGroupedNumber, ORDERSTATE);
					runOnUIThread("请注意！！！！！！！", "组对码错误，正确的组对码是：" + otherGroupedNumber);
//					dismissAlertDialog(builder);										
				}

				groupstatus = 6;// 确保回到这个状态
			}
			break;
		case 7:// 收到组对通知后已答复，等待组对完成
			Log.e("zgm", "1213：groupstatus=" + groupstatus);
			// 等待
			break;
		case 8:// 建组失败
			Log.e("zgm", "1213：groupstatus=" + groupstatus);
			// 等待抬笔取消建组
			break;
		case 9:// 建组成功后抬笔的状态,和groupstatus=0一样，因为要求是组队完成后依然可以组队(顶替现有的组对)
			Log.e("zgm", "1213：groupstatus=" + groupstatus);
			if (System.currentTimeMillis() - firstTime > 3000) {
				groupedNumber = OIDSize;
				// 发送组对请求
				StringMessage mStringMessage = new StringMessage(GROUP_REQUEST, "" + OIDSize, mNameString);
				byte[] mbyte = ObjAndByte.ObjectToByte(mStringMessage);
				sendMessageManyTimes(mbyte);
				groupstatus = 1;// 状态改变
				groupLeader = 1;
				if (firstOccur) {
					firstOccur = false;
					runOnUIThread("建组状态通知!", "你进入组对状态，组对码是：" + groupedNumber);
//						builder.setTitle("建组状态通知!");
//						builder.setMessage("你进入组对状态，组对码是："+groupedNumber);
					builder.show();
					updateUsingInfo("你进入组对状态，组对码是：" + groupedNumber, ORDERSTATE);
					showSound(R.raw.in);
					showVibrator();// 震动
				}

			}
			break;
		default:
			return;
		}

	}

	// 判断表是否存在
	private boolean IsTableExist(String databseNameString, String tableName) {
		boolean isTableExist = true;
//				Log.e("zgm", "20181222:表存在吗？");
		smartPenDatabase = openOrCreateDatabase(databseNameString, 0, null);
		Cursor c = smartPenDatabase
				.rawQuery("SELECT count(*) FROM sqlite_master WHERE type='table' AND name='" + tableName + "'", null);

		if (c.moveToFirst()) {
//					  Log.e("zgm", "20181222:c.getCount()="+c.getInt(0));
			if (c.getInt(0) == 0) {
				isTableExist = false;
			}
		}

//				c.close();  
//				smartPenDatabase.close();
		return isTableExist;
	}

	public void dealDot(final Dot dot) {
		if (!isDealPenPoint) {
			return;
		}
		if (dot.type == DotType.PEN_DOWN) {
			g_x0 = (float) (dot.x + dot.fx / 100.0);
			g_y0 = (float) (dot.y + dot.fy / 100.0);

			if (gCurBookID != dot.BookID || gCurPageID != dot.PageID) {
				String sqlstr = "";
				gCurBookID = dot.BookID;
				gCurPageID = dot.PageID;
				switch (homeworkStatus) {

				case 0:// 其他状态
					Log.e("zgm", "homeworkStatus=" + homeworkStatus + "其他状态");
					break;
				case 1:// 作答状态，默认//此时要更新pageid,bookid,和学号的对应关系并保存在数据库中
					Log.e("zgm", "homeworkStatus=" + homeworkStatus + "作答状态");
					/*
					 * sqlstr="REPLACE INTO homeworkpageandstudentinfo (bookid,pageid,studentNumber) VALUES (?,?,?)"
					 * ; Object[] args = new Object[]
					 * {dot.BookID+"",dot.PageID+"",studentNumber+""}; try{
					 * 
					 * smartPenDatabase.execSQL(sqlstr,args);
					 * 
					 * } catch (SQLException ex) { Log.e("zgm",
					 * "0117:ex.getMessage():"+ex.getMessage()); }
					 */
					break;
				case 2:// 作业正在批改状态
					Log.e("zgm", "homeworkStatus=" + homeworkStatus + "批改状态");

					break;
				case 3:// 订正状态，此时要根据pageid,bookid来从数据库中找学号
					Log.e("zgm", "homeworkStatus=" + homeworkStatus + "订正状态");
					/*
					 * mHandler.sendEmptyMessage(0417);//message.what=0417; Log.e("zgm",
					 * "homeworkStatus="+homeworkStatus+"订正状态");
					 * sqlstr="SELECT studentNumber FROM homeworkpageandstudentinfo WHERE bookid=? AND pageid=?"
					 * ; try{ Cursor mCursor =smartPenDatabase.rawQuery(sqlstr,new
					 * String[]{dot.BookID+"",dot.PageID+""}); if (mCursor.getCount()==0)
					 * {//没有找到信息，那么就自动进入作答状态 homeworkStatus=1;
					 * gCurBookID=-1;//为了强制进入homeworkStatus=1的作答状态 return; }else {
					 * mCursor.moveToFirst();
					 * studentNumber=mCursor.getString(mCursor.getColumnIndex("studentNumber"));
					 * Log.e("zgm",
					 * "student:"+studentNumber+" dot.BookID:"+dot.BookID+" dot.PageID:"+dot.PageID)
					 * ;
					 *//**
						 * 下载文件的操作
						 */
					/*
					 * // showDownLoadDialog();
					 * 
					 * // mHandler.sendEmptyMessage(0417);//message.what=0417;
					 *//**
						 * 重画图操作
						 *//*
							 * } } catch (SQLException ex) { Log.e("zgm",
							 * "0117:ex.getMessage():"+ex.getMessage()); return ; }
							 */
					break;
				default:
					break;
				}
			}
		}

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				/*
				 * Log.i("zgm", "Dot信息,BookID" + dot.BookID); Log.i("zgm", "Dot信息,ab_x" +
				 * dot.ab_x);
				 */

				if (!correcting) {// 不是小组互批状态
					ProcessDots(dot);
				}
				if (groupstatus == 9 && correcting) {// 如果组队成功，那么将数据发送 { byte[]

					byte[] dotByte = ObjAndByte.ObjectToByte(dot);
					/*
					 * if (dot.type==Dot.DotType.PEN_DOWN) { sendMessageManyTimes(dotByte); }
					 */
					sendMessageManyTimes(dotByte);
				}

			}
		});

	}

//wsk 2019.1.26
//读题目和三维语义
	/**
	 * 根据题号和题号对应题目的区域阅读题目
	 * 
	 * @param tag
	 */

	public void initGroupPeople() {
		groupPeople1.setBackgroundResource(R.drawable.backgroundcolor);
		groupPeople2.setBackgroundResource(R.drawable.backgroundcolor);
//	groupPeople1.setBackgroundResource(R.drawable.backgroundcolor);
	}

	private void doDownLoadWork() {
		/*
		 * DownLoaderTask task = new DownLoaderTask("http://" + inputIp +
		 * "/Public/Uploads/"+filename, "/sdcard/xyz/", this);
		 */
		Log.e("zgm", "studentdoen:" + studentNumber);
		DownLoaderTask task = new DownLoaderTask(
				"http://118.24.109.3/Public/smartpen/download.php?sid=" + studentNumber, "/sdcard/xyz/", this);
		// DownLoaderTask task = new
		// DownLoaderTask("http://192.168.9.155/johnny/test.h264",
		// getCacheDir().getAbsolutePath()+"/", this);
		task.execute();
	}

	public void showUnzipDialog() {
		new AlertDialog.Builder(this).setTitle("确认").setMessage("是否解压？")
				.setPositiveButton("是", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						// TODO Auto-generated method stub
						Log.d(TAG, "onClick 1 = " + which);
						doZipExtractorWork();
					}
				}).setNegativeButton("否", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Log.d(TAG, "onClick 2 = " + which);
					}
				}).show();
	}

	private void showDownLoadDialog() {
		new AlertDialog.Builder(this).setTitle("确认").setMessage("是否下载？")
				.setPositiveButton("是", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Log.d(TAG, "onClick 1 = " + which);
						doDownLoadWork();
					}
				}).setNegativeButton("否", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Log.d(TAG, "onClick 2 = " + which);
					}
				}).show();
	}

	public void doDrawFromFile() {
		for (String fileName : filenames) {
			new DrawFromFileTask(fileName, bDrawl[0], this).execute();
		}

	}

	public void doZipExtractorWork() {
		// ZipExtractorTask task = new
		// ZipExtractorTask("/storage/usb3/system.zip",
		// "/storage/emulated/legacy/", this, true);
		ZipExtractorTask task = new ZipExtractorTask("/storage/emulated/0/xyz/download.zip", "/storage/emulated/0/xyz",
				this, true);
		task.execute();
		Uri data = Uri.parse("file://storage/emulated/0/");
		this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, data));

	}

	/**
	 * 
	 * @param path ：要遍历的文件夹的路径(是文件夹的路径)
	 * @return 该文件夹及其子文件夹的文件(不一定是全部文件)
	 */
// 用于遍历文件价
	public ArrayList<String> iteratorPath(String dirPath, String studentNumber, int bookid, int pageid) {
		File or = new File(dirPath);
		File[] files = or.listFiles();
		ArrayList<String> fileName = new ArrayList<String>();
		if (files == null) {
			return fileName;
		}
		String regExpressPattern = "^(?:NONE|001)-" + studentNumber + "-" + bookid + "-" + pageid + "-0.page";// 正则表达式
		Pattern pattern = Pattern.compile(regExpressPattern);
		Matcher matcher;
		for (File file : files) {
			if (file.isFile()) {
				matcher = pattern.matcher(file.getName());
				Log.e("zgm", file.getName());
				boolean rs = matcher.matches();
				if (rs) {
					fileName.add(file.getName());
				}
			} else if (file.isDirectory()) {
				iteratorPath(file.getAbsolutePath(), studentNumber, bookid, pageid);
			}
		}
		return fileName;
	}

	public void ReadVideo(int OIDSize) {
		String path = "/storage/emulated/0/yinpin/";
		showSound(path + "video" + OIDSize + ".mp3");
	}

	/**
	 * 
	 * @param path:要删除内容的文件夹
	 * @return
	 */

	public static boolean deleteDir(String path) {
		File file = new File(path);
		if (!file.exists()) {// 判断是否待删除目录是否存在
			System.err.println("The dir are not exists!");
			return false;
		}

		String[] content = file.list();// 取得当前目录下所有文件和文件夹
		for (String name : content) {
			File temp = new File(path, name);
			if (temp.isDirectory()) {// 判断是否是目录
				deleteDir(temp.getAbsolutePath());// 递归调用，删除目录里的内容
				temp.delete();// 删除空目录
			} else {
				if (!temp.delete()) {// 直接删除文件
					System.err.println("Failed to delete " + name);
				}
			}
		}
		return true;
	}

	/**
	 * 初始化最小显示版面变量
	 */
	public void initMiniShowAreaVariable() {
		minishowlayout = (RelativeLayout) findViewById(R.id.minishow);
		leftarrow = (ImageView) findViewById(R.id.leftarrow);
		rightarrow = (ImageView) findViewById(R.id.rightarrow);
		selfWriteStatus = (RelativeLayout) findViewById(R.id.selfwrite);// 自我书写状态
		verbal = (RelativeLayout) findViewById(R.id.verbal);
		math = (RelativeLayout) findViewById(R.id.math);
		english = (RelativeLayout) findViewById(R.id.english);
		groupStatus = (RelativeLayout) findViewById(R.id.groupvalue);
	}

	/**
	 * 初始化最小显示界面控件为隐藏
	 */
	public void initMiniShowAreaVisibility() {
		selfWriteStatus.setVisibility(View.GONE);
		verbal.setVisibility(View.GONE);
		math.setVisibility(View.GONE);
		english.setVisibility(View.GONE);
		groupStatus.setVisibility(View.GONE);
	}

	/**
	 * 根据序号确定最小显示版面中的哪一个布局控件显示
	 * 
	 * @param index
	 */
	public void setMiniShowViewShow(int index) {
		initMiniShowAreaVisibility();
		switch (index) {
		case 0:
			selfWriteStatus.setVisibility(View.VISIBLE);
			break;
		case 1:
			verbal.setVisibility(View.VISIBLE);
			break;
		case 2:
			math.setVisibility(View.VISIBLE);
			break;
		case 3:
			english.setVisibility(View.VISIBLE);
			break;
		case 4:
			groupStatus.setVisibility(View.VISIBLE);
			break;

		default:
			break;
		}
	}

	public void updateInfo(float f, int category) {
		switch (category) {
		case 1:
			taillength = f + "";
			break;
		case 2:
			gesturelength = f + "";
			break;
		case 3:
			boundingBoxWidth = f + "";
			break;
		case 4:
			boundingBoxheight = f + "";
			break;
		case 5:
			taileChangeTimes = (int) f + "";
			break;
		case 6:
			tailPointCounter = (int) f + "";
			break;
		case 7:
			tailSlope = -f + "";
			break;
		case 8:
			physicsTaillength = f + "";
			break;
		case 9:
			physicsgesturelength = f + "";
			break;
		case 10:
			centerX = f + "";
			break;
		case 11:
			centerY = f + "";
			break;
		case 12:
			 bodySlope = f + "";
			break;
		case 13:
			bodyAndBodyBoxRation = f + "";
			break;
			
		default:
			break;
		}
		gestureInfoString = "尾部长度:" + taillength + "\n" + "尾部物理长度(mm)：" + physicsTaillength + "\n"+ "手势整体长度：" + gesturelength + "\n"+ "手势整体物理长度(mm)：" + physicsgesturelength + "\n" + "主体边框的宽："
				+ boundingBoxWidth + "\n" + "主体边框的高：" + boundingBoxheight + "\n" + "尾巴的斜率:" + tailSlope + "\n"
				+ "尾巴的变化次数：" + taileChangeTimes + "\n" + "尾巴的点的个数：" + tailPointCounter+ "\n" + "中心点坐标(mm)：" + centerX+"  "+centerY+ "\n" + "主体点集斜率：" + bodySlope+"\n" + "主体和主体边框的比值：" + bodyAndBodyBoxRation;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				gestureinfo.setText(gestureInfoString);
			}
		});

	}

	public void drawLine(final float x0, final float y0, final float x1, final float y1) {

		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				bDrawl[0].paint.setColor(Color.BLUE);
				bDrawl[0].canvas.drawLine(x0, y0, x1, y1, bDrawl[0].paint);
				bDrawl[0].invalidate();
//			bDrawl[0].canvas.drawLine(60, 100, 140, 100, drawView.paint);
				bDrawl[0].paint.setColor(Color.RED);
			}
		});

	}

	public Map<String, ArrayList<SimplePoint>> getGestureDataFromfile(String Gesturename, int counter)
			throws IOException, ClassNotFoundException {
		Map<String, ArrayList<SimplePoint>> mapGesture = null;
		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Gesturename
				+ File.separator + counter + ".txt";
		Log.i("di", "id" + path);
		File file = new File(path);
		if (!file.exists()) {
			return mapGesture;
		} else {
			long fileSize = file.length();
			InputStream input = new FileInputStream(file);
			byte[] buf = new byte[(int) fileSize];
			input.read(buf);
			ByteArrayInputStream bis = new ByteArrayInputStream(buf);
			ObjectInputStream ois = new ObjectInputStream(bis);
			mapGesture = (Map<String, ArrayList<SimplePoint>>) ois.readObject();
			return mapGesture;

		}
	}
}