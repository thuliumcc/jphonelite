package javaforce.jna.com;

/**
 * GUID
 *
 * @author pquiring
 *
 * Copied from JNA : Aug 17, 2013
 */

import java.security.SecureRandom;
import java.util.*;

import com.sun.jna.*;

/**
 * Ported from Guid.h. Microsoft Windows SDK 6.0A.
 *
 * @author dblock[at]dblock.org
 */

public interface Guid {

  public final static CLSID CLSID_SystemDeviceEnum = new CLSID
    ("{62BE5D10-60EB-11d0-BD-3B-00-A0-C9-11-CE-86}");
  public final static CLSID CLSID_VideoInputDeviceCategory = new CLSID
    ("{860BB310-5D01-11d0-BD-3B-00-A0-C9-11-CE-86}");
  public final static CLSID CLSID_CaptureGraphBuilder2 = new CLSID
    ("{BF87B6E1-8C27-11d0-B3-F0-00-AA-00-37-61-C5}");
  public final static CLSID CLSID_FilterGraph = new CLSID
    ("{e436ebb3-524f-11ce-9f-53-00-20-af-0b-a7-70}");
  public final static CLSID CLSID_SampleGrabber = new CLSID
    ("{C1F400A0-3F08-11D3-9F0B-006008039E37}");  //retrieved from my registry
  public final static CLSID CLSID_NullRenderer = new CLSID
    ("{C1F400A4-3F08-11D3-9F0B-006008039E37}");  //retrieved from my registry

  public final static IID IID_NULL = new IID();
  public final static IID IID_ICreateDevEnum = new IID
    ("{29840822-5b84-11d0-bd-3b-00-a0-c9-11-ce-86}");
  public final static IID IID_IPropertyBag = new IID
    ("{55272a00-42cb-11ce-81-35-00-aa-00-4b-b8-51}");
  public final static IID IID_ICaptureGraphBuilder2 = new IID
    ("{93e5a4e0-2d50-11d2-ab-fa-00-a0-c9-c6-e3-8d}");
  public final static IID IID_IGraphBuilder = new IID
    ("{56a868a9-0ad4-11ce-b0-3a-00-20-af-0b-a7-70}");
  public final static IID IID_IMediaControl = new IID
    ("{56a868b1-0ad4-11ce-b0-3a-00-20-af-0b-a7-70}");
  public final static IID IID_IBaseFilter = new IID
    ("{56a86895-0ad4-11ce-b0-3a-00-20-af-0b-a7-70}");
  public final static IID IID_IAMStreamConfig = new IID
    ("{c6e13340-30ac-11d0-a1-8c-00-a0-c9-11-89-56}");
  public final static IID IID_ISampleGrabber = new IID
    ("{6b652fff-11fe-4fce-92-ad-02-66-b5-d7-c7-8f}");
  public final static IID IID_IPin = new IID
    ("{56a86891-0ad4-11ce-b0-3a-00-20-af-0b-a7-70}");

  public final static GUID PIN_CATEGORY_PREVIEW = new GUID
    ("{fb6c4282-0353-11d1-90-5f-00-00-c0-cc-16-ba}");
  public final static GUID PIN_CATEGORY_CAPTURE = new GUID
    ("{fb6c4281-0353-11d1-90-5f-00-00-c0-cc-16-ba}");
  public final static GUID MEDIATYPE_Video = new GUID
    ("{73646976-0000-0010-80-00-00-aa-00-38-9b-71}");
  public final static GUID FORMAT_VideoInfo = new GUID
    ("{05589f80-c356-11ce-bf-01-00-aa-00-55-59-5a}");
  public final static GUID MEDIASUBTYPE_RGB24 = new GUID
    ("{e436eb7d-524f-11ce-9f-53-00-20-af-0b-a7-70}");
  public final static GUID MEDIASUBTYPE_RGB32 = new GUID
    ("{e436eb7e-524f-11ce-9f-53-00-20-af-0b-a7-70}");

	/**
	 * The Class GUID.
	 */
	public static class GUID extends Structure {

		/**
		 * The Class ByReference.
		 */
		public static class ByReference extends GUID implements
				Structure.ByReference {

			/**
			 * Instantiates a new by reference.
			 */
			public ByReference() {
			}

			/**
			 * Instantiates a new by reference.
			 *
			 * @param guid
			 *            the guid
			 */
			public ByReference(GUID guid) {
				super(guid.getPointer());

				Data1 = guid.Data1;
				Data2 = guid.Data2;
				Data3 = guid.Data3;
				Data4 = guid.Data4;
			}

			/**
			 * Instantiates a new by reference.
			 *
			 * @param memory
			 *            the memory
			 */
			public ByReference(Pointer memory) {
				super(memory);
			}
		}

		/** The Data1. */
		public int Data1;

		/** The Data2. */
		public short Data2;

		/** The Data3. */
		public short Data3;

		/** The Data4. */
		public byte[] Data4 = new byte[8];

		/**
		 * Instantiates a new guid.
		 */
		public GUID() {
		}

		/**
		 * Instantiates a new guid.
		 *
		 * @param guid
		 *            the guid
		 */
		public GUID(GUID guid) {
			this.Data1 = guid.Data1;
			this.Data2 = guid.Data2;
			this.Data3 = guid.Data3;
			this.Data4 = guid.Data4;

			this.writeFieldsToMemory();
		}

		/**
		 * Instantiates a new guid.
		 *
		 * @param guid
		 *            the guid
		 */
		public GUID(String guid) {
			this(fromString(guid));
		}

		/**
		 * Instantiates a new guid.
		 *
		 * @param data
		 *            the data
		 */
		public GUID(byte[] data) {
			this(fromBinary(data));
		}

		/**
		 * Instantiates a new guid.
		 *
		 * @param memory
		 *            the memory
		 */
		public GUID(Pointer memory) {
			super(memory);
			read();
		}

		/**
		 * From binary.
		 *
		 * @param data
		 *            the data
		 * @return the guid
		 */
		public static GUID fromBinary(byte[] data) {
			if (data.length != 16) {
				throw new IllegalArgumentException("Invalid data length: "
						+ data.length);
			}

			GUID newGuid = new GUID();
			long data1Temp = data[0] & 0xff;
			data1Temp <<= 8;
			data1Temp |= data[1] & 0xff;
			data1Temp <<= 8;
			data1Temp |= data[2] & 0xff;
			data1Temp <<= 8;
			data1Temp |= data[3] & 0xff;
			newGuid.Data1 = (int) data1Temp;

			int data2Temp = data[4] & 0xff;
			data2Temp <<= 8;
			data2Temp |= data[5] & 0xff;
			newGuid.Data2 = (short) data2Temp;

			int data3Temp = data[6] & 0xff;
			data3Temp <<= 8;
			data3Temp |= data[7] & 0xff;
			newGuid.Data3 = (short) data3Temp;

			newGuid.Data4[0] = data[8];
			newGuid.Data4[1] = data[9];
			newGuid.Data4[2] = data[10];
			newGuid.Data4[3] = data[11];
			newGuid.Data4[4] = data[12];
			newGuid.Data4[5] = data[13];
			newGuid.Data4[6] = data[14];
			newGuid.Data4[7] = data[15];

			newGuid.writeFieldsToMemory();

			return newGuid;
		}

		/**
		 * From string.
		 *
		 * @param guid
		 *            the guid
		 * @return the guid
		 */
		public static GUID fromString(String guid) {
			int y = 0;
			char[] _cnewguid = new char[32];
			char[] _cguid = guid.toCharArray();
			byte[] bdata = new byte[16];
			GUID newGuid = new GUID();

			// we not accept a string longer than 44 chars
			if (guid.length() > 44) {
				throw new IllegalArgumentException("Invalid guid length: "
						+ guid.length());
			}

			// remove '{', '}' and '-' from guid string
			for (int i = 0; i < _cguid.length; i++) {
				if ((_cguid[i] != '{') && (_cguid[i] != '-')
						&& (_cguid[i] != '}'))
					_cnewguid[y++] = _cguid[i];
			}

			// convert char to byte
			for (int i = 0; i < 32; i += 2) {
				bdata[i / 2] = (byte) ((Character.digit(_cnewguid[i], 16) << 4)
						+ Character.digit(_cnewguid[i + 1], 16) & 0xff);
			}

			if (bdata.length != 16) {
				throw new IllegalArgumentException("Invalid data length: "
						+ bdata.length);
			}

			long data1Temp = bdata[0] & 0xff;
			data1Temp <<= 8;
			data1Temp |= bdata[1] & 0xff;
			data1Temp <<= 8;
			data1Temp |= bdata[2] & 0xff;
			data1Temp <<= 8;
			data1Temp |= bdata[3] & 0xff;
			newGuid.Data1 = (int) data1Temp;

			int data2Temp = bdata[4] & 0xff;
			data2Temp <<= 8;
			data2Temp |= bdata[5] & 0xff;
			newGuid.Data2 = (short) data2Temp;

			int data3Temp = bdata[6] & 0xff;
			data3Temp <<= 8;
			data3Temp |= bdata[7] & 0xff;
			newGuid.Data3 = (short) data3Temp;

			newGuid.Data4[0] = bdata[8];
			newGuid.Data4[1] = bdata[9];
			newGuid.Data4[2] = bdata[10];
			newGuid.Data4[3] = bdata[11];
			newGuid.Data4[4] = bdata[12];
			newGuid.Data4[5] = bdata[13];
			newGuid.Data4[6] = bdata[14];
			newGuid.Data4[7] = bdata[15];

			newGuid.writeFieldsToMemory();

			return newGuid;
		}

		/**
		 * Generates a new guid. Code taken from the standard jdk
		 * implementation (see UUID class).
		 *
		 * @return the guid
		 */
		public static GUID newGuid() {
			SecureRandom ng = new SecureRandom();
			byte[] randomBytes = new byte[16];

			ng.nextBytes(randomBytes);
			randomBytes[6] &= 0x0f;
			randomBytes[6] |= 0x40;
			randomBytes[8] &= 0x3f;
			randomBytes[8] |= 0x80;

			return new GUID(randomBytes);
		}

		/**
		 * To byte array.
		 *
		 * @return the byte[]
		 */
		public byte[] toByteArray() {
			byte[] guid = new byte[16];

			byte[] bytes1 = new byte[4];
			bytes1[0] = (byte) (Data1 >> 24);
			bytes1[1] = (byte) (Data1 >> 16);
			bytes1[2] = (byte) (Data1 >> 8);
			bytes1[3] = (byte) (Data1 >> 0);

			byte[] bytes2 = new byte[4];
			bytes2[0] = (byte) (Data2 >> 24);
			bytes2[1] = (byte) (Data2 >> 16);
			bytes2[2] = (byte) (Data2 >> 8);
			bytes2[3] = (byte) (Data2 >> 0);

			byte[] bytes3 = new byte[4];
			bytes3[0] = (byte) (Data3 >> 24);
			bytes3[1] = (byte) (Data3 >> 16);
			bytes3[2] = (byte) (Data3 >> 8);
			bytes3[3] = (byte) (Data3 >> 0);

			System.arraycopy(bytes1, 0, guid, 0, 4);
			System.arraycopy(bytes2, 2, guid, 4, 2);
			System.arraycopy(bytes3, 2, guid, 6, 2);
			System.arraycopy(Data4, 0, guid, 8, 8);

			return guid;
		}

		/**
		 * The value of this Guid, formatted as follows:
		 * xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx.
		 *
		 * @return the string
		 */
		public String toGuidString() {
			final String HEXES = "0123456789ABCDEF";
			byte[] bGuid = toByteArray();

			final StringBuilder hexStr = new StringBuilder(2 * bGuid.length);
			hexStr.append("{");

			for (int i = 0; i < bGuid.length; i++) {
				char ch1 = HEXES.charAt((bGuid[i] & 0xF0) >> 4);
				char ch2 = HEXES.charAt(bGuid[i] & 0x0F);
				hexStr.append(ch1).append(ch2);

				if ((i == 3) || (i == 5) || (i == 7) || (i == 9))
					hexStr.append("-");
			}

			hexStr.append("}");
			return hexStr.toString();
		}

		/**
		 * Write fields to backing memory.
		 */
		protected void writeFieldsToMemory() {
			this.writeField("Data1");
			this.writeField("Data2");
			this.writeField("Data3");
			this.writeField("Data4");
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see com.sun.jna.Structure#getFieldOrder()
		 */
		protected List getFieldOrder() {
			return Arrays.asList(new String[] { "Data1", "Data2", "Data3", "Data4" });
		}
	}

	/**
	 * The Class CLSID.
	 */
	public static class CLSID extends GUID {

		/**
		 * The Class ByReference.
		 */
		public static class ByReference extends GUID {

			/**
			 * Instantiates a new by reference.
			 */
			public ByReference() {
			}

			/**
			 * Instantiates a new by reference.
			 *
			 * @param guid
			 *            the guid
			 */
			public ByReference(GUID guid) {
				super(guid);
			}

			/**
			 * Instantiates a new by reference.
			 *
			 * @param memory
			 *            the memory
			 */
			public ByReference(Pointer memory) {

			}
		}

		/**
		 * Instantiates a new clsid.
		 */
		public CLSID() {
		}

    /** Instantiates from a string */
    public CLSID(String string) {
      super(string);
    }
	}

	/**
	 * The Class REFIID.
	 */
	public class REFIID extends IID {

		/**
		 * Instantiates a new refiid.
		 */
		public REFIID() {
			// TODO Auto-generated constructor stub
		}

		/**
		 * Instantiates a new refiid.
		 *
		 * @param memory
		 *            the memory
		 */
		public REFIID(Pointer memory) {
			super(memory);
			// TODO Auto-generated constructor stub
		}

		/**
		 * Instantiates a new refiid.
		 *
		 * @param data
		 *            the data
		 */
		public REFIID(byte[] data) {
			super(data);
			// TODO Auto-generated constructor stub
		}

	}

	/**
	 * The Class IID.
	 */
	public class IID extends GUID {

		/**
		 * Instantiates a new iid.
		 */
		public IID() {
			// TODO Auto-generated constructor stub
		}

		/**
		 * Instantiates a new iid.
		 *
		 * @param memory
		 *            the memory
		 */
		public IID(Pointer memory) {
			super(memory);
			// TODO Auto-generated constructor stub
		}

		public IID(String iid) {
			super(iid);
			// TODO Auto-generated constructor stub
		}

		/**
		 * Instantiates a new iid.
		 *
		 * @param data
		 *            the data
		 */
		public IID(byte[] data) {
			super(data);
			// TODO Auto-generated constructor stub
		}
	}
}
