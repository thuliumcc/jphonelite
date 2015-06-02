package javaforce.jna;

/**
 * FFMPEG API
 *
 * Compatible with ffmpeg.org and libav.org
 *
 * @author pquiring
 *
 * Created : 8/13/2013
 *
 */

import com.sun.jna.*;
import com.sun.jna.ptr.*;

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.net.*;
import java.util.zip.*;

import javaforce.*;

public class FFMPEG {
  //NOTE:Most of these Structures are INCOMPLETE - Do not create them directly.
  //Create them with a pointer to pre-alloced memory.
  //Only AVPacket and AVPicture are complete since ffmpeg has no func to create them.
  public static class AVFormatContext extends Structure {
    public Pointer av_class, iformat, oformat, priv_data, pb;
    public int ctx_flags;
    public int nb_streams;
    public Pointer streams;
    public byte filename[] = new byte[1024];
    public long start_time;
    public long duration;
/*
    public int bitrate;
    public int packet_size;
    public int max_delay;
    public int flags;
    public int probesize;
    public int max_a_d;
    public Pointer key;
    public int key_len;
    public int nb_programs;
    public Pointer programs;
    public int video_codec_id;
    public int audio_codec_id;
    public int subtitle_codec_id;
    public int max_index_size;
    public int max_picture_buffer;
    public int nb_chapters;
    public Pointer chapters;
    public Pointer metadata;
    //...etc...
*/
    public int _fieldOffset(String f) {
      return fieldOffset(f);
    }
    //...etc...
    protected List getFieldOrder() {
      return makeFieldList(getClass());
    }
    public AVFormatContext(Pointer ptr) {
      super(ptr);
    }
  }
  public static class AVStream extends Structure {
    public int index, id;
    public Pointer codec;
    public Pointer priv_data;
    //struct AVFrac {
      public long pts_val;
      public long pts_num;
      public long pts_den;
    //}
    //public AVRational time_base;
    public int time_base_num, time_base_den;
    //...etc...
    protected List getFieldOrder() {
      return makeFieldList(getClass());
    }
    public AVStream(Pointer ptr) {
      super(ptr);
    }
  }
  public static class AVCodec extends Structure {
    public Pointer name, long_name;
    public int type;
    //...etc...
    protected List getFieldOrder() {
      return makeFieldList(getClass());
    }
    public AVCodec(Pointer ptr) {
      super(ptr);
    }
  }
  public static class AVClass extends Structure {
    public Pointer name;
    protected List getFieldOrder() {
      return makeFieldList(getClass());
    }
    public AVClass(Pointer ptr) {
      super(ptr);
    }
  }
  public static class AVCodecContext extends Structure {
    public Pointer av_class;
    public int log_level_offset;
    public int codec_type;
    public Pointer codec;
    public byte codec_name[] = new byte[32];
    public int codec_id;
    public int codec_tag;
    public int stream_codec_tag;
    public Pointer priv_data;
    public Pointer internal;
    public Pointer opaque;
    public int bit_rate;
    public int bit_rate_tolerance;
    public int global_quality;
    public int compression_level;
    public int flags;
    public int flags2;
    public Pointer extra_data;
    public int extra_data_size;
//    public AVRational time_base;
    public int time_base_num, time_base_den;
    public int ticks_per_frame;
    public int delay;
    public int width, height;
    public int coded_width, coded_height;
    public int gop_size;
    public int pix_fmt;
    public int me_method;
    public Pointer draw_band;
    public Pointer get_format;
    public int max_b_frames;
    public float b_quant_factor;
    public int rc_strategy;
    public int b_frame_strategy;
    public float b_quant_offset;
    public int has_b_frames;
    public int mpeg_quant;
    public float i_quant_factor;
    public float i_quant_offset;
    public float lumi_masking;
    public float temporal_cplx_masking;
    public float spatial_cplx_masking;
    public float p_masking;
    public float dark_masking;
    public int slice_count;
    public int prediction_method;
    public Pointer slice_offset;
//    public AVRational sample_aspect_ratio;
    public int sample_aspect_num, sample_aspect_den;
    public int me_cmp;
    public int me_sub_cmp;
    public int mb_cmp;
    public int ildct_cmp;
    public int dia_size;
    public int last_predictor_count;
    public int pre_me;
    public int me_pre_cmp;
    public int pre_dia_size;
    public int me_subpel_quality;
    public int dtg_active_format;
    public int me_range;
    public int intra_quant_bias;
    public int inter_quant_bias;
    public int slice_flags;
    public int xvmc_acceleration;
    public int mb_decision;
    public Pointer intra_matrix;
    public Pointer inter_matrix;
    public int scenechange_threshold;
    public int noise_reduction;
    public int me_threshold;
    public int mb_threshold;
    public int intra_dc_precision;
    public int skip_top;
    public int skip_bottom;
    public float border_masking;
    public int mb_lmin;
    public int mb_lmax;
    public int me_penalty_compensation;
    public int bidir_refine;
    public int brd_scale;
    public int keyint_min;
    public int refs;
    public int chromaoffset;
    public int scenechange_factor;
    public int mv0_threshold;
    public int b_sensitivity;
    public int color_primaries;
    public int color_trc;
    public int colorspace;
    public int color_range;
    public int chroma_sample_location;
    public int slices;
    public int field_order;
    public int sample_rate;
    public int channels;
    public int sample_fmt;
    public int frame_size;
    public int frame_number;
    public int block_align;
    public int cutoff;
    public long channel_layout;
    //...etc...

    public int fieldOffset(String field) {
      return super.fieldOffset(field);
    }
    protected List getFieldOrder() {
      return makeFieldList(getClass());
    }
    public AVCodecContext(Pointer ptr) {
      super(ptr);
    }
  }
  public static class AVPacket extends Structure {
    public Pointer buf;
    public long pts;
    public long dts;
    public Pointer data;
    public int size, stream_index, flags;
    public Pointer side_data;  //pointer to struct
    public int side_data_elems;
    public int duration;
    //#ifdef FF_API_DESTRUCT_PACKET  //(if LIBAVCODEC_VERSION_MAJOR < 56)
    public Pointer destruct;
    public Pointer priv;
    //#endif
    public long pos;
    public long convergence_duration;
    protected List getFieldOrder() {
      return makeFieldList(getClass());
    }
    public AVPacket() {}
    public AVPacket(Pointer ptr) {
      super(ptr);
    }
  }
  public static class AVFrame extends Structure {
    public Pointer data[] = new Pointer[8];
    public int linesize[] = new int[8];
    public Pointer extended_data;  //array
    public int width, height;
    public int nb_samples;
    public int format;
    public int key_frame;
    public int pict_type;
    public Pointer base[] = new Pointer[8];
//    public AVRational sample_aspect_ratio;
    public int sample_aspect_num, sample_aspect_den;
    public long pts;
    public long pkt_pts;
    public long pkt_dts;
    public int coded_picture_number, display_picture_number, quality, reference;
    public Pointer qscale_table;
    public int qstride, qscale_type;
    public Pointer mbskip_table;
    public Pointer motion_val[] = new Pointer[2];
    public Pointer mb_type, dct_coeff;
    public Pointer ref_index[] = new Pointer[2];
    public Pointer opaque;
    public long error[] = new long[8];
    public int type, repeat_pict, interlaced_frame, top_field_first, palette_has_changed, buffer_hints;
    public Pointer pan_scan;
    public long reordered_opaque;
    public Pointer hwaccel_picture_private, owner, thread_opaque;
    public byte motion_subsample_log2;
    public int sample_rate;
    public long channel_layout;
    //...etc...
    protected List getFieldOrder() {
      return makeFieldList(getClass());
    }
    public AVFrame(Pointer ptr) {
      super(ptr);
    }
  }
  public static class AVOutputFormat extends Structure {
    public String name, long_name, mime_type, extensions;
    public int audio_codec, video_codec, subtitle_codec, flags;
    public Pointer codec_tag, priv_class, next;
    public int priv_data_size;
    public Pointer write_header, write_packet, write_trailer, interleave_packet, query_codec, get_output_timestamp;
    protected List getFieldOrder() {
      return makeFieldList(getClass());
    }
    public AVOutputFormat(Pointer ptr) {
      super(ptr);
    }
    public AVOutputFormat() {}
  }
  public static class AVPicture extends Structure {
    public Pointer data[] = new Pointer[8];
    public int linesize[] = new int[8];
    protected List getFieldOrder() {
      return makeFieldList(getClass());
    }
    public AVPicture(Pointer ptr) {
      super(ptr);
    }
    public AVPicture() {}
  }
  //NOTE:This structure is NOT passed as a pointer to functions (ByValue)
  public static class AVRational extends Structure implements Structure.ByValue {
    public int num, den;
    protected List getFieldOrder() {
      return makeFieldList(getClass());
    }
    public AVRational(Pointer ptr) {
      super(ptr);
    }
    public AVRational() {}
  }
  public static class AVInputFormat extends Structure {
    public String name;
    //...etc...
    protected List getFieldOrder() {
      return makeFieldList(getClass());
    }
    public AVInputFormat(Pointer ptr) {
      super(ptr);
    }
    public AVInputFormat() {}
  }

/*
  public static class AVIOContext extends Structure {
    public Pointer av_class;
    public Pointer buffer;
    public int buffer_size;
    public Pointer buf_ptr;
    public Pointer buf_end;
    public Pointer opaque;
    public Pointer read, write, seek;
    public long pos;

    protected List getFieldOrder() {
      return makeFieldList(getClass());
    }
    public AVIOContext(Pointer ptr) {
      super(ptr);
    }
    public AVIOContext() {}
  }
*/

  private static List makeFieldList(Class cls) {
    //This "assumes" compiler places fields in order as defined (some don't)
    ArrayList<String> list = new ArrayList<String>();
    Field fields[] = cls.getFields();
    for(int a=0;a<fields.length;a++) {
      String name = fields[a].getName();
      if (name.startsWith("ALIGN_")) continue;  //field of Structure
      list.add(name);
    }
    return list;
  }

  //constants
  private static final int AVMEDIA_TYPE_VIDEO = 0;
  private static final int AVMEDIA_TYPE_AUDIO = 1;
  private static final int SWS_BILINEAR = 2;
  private static final int AV_CH_LAYOUT_MONO = 4;
  private static final int AV_CH_LAYOUT_STEREO = 3;
  private static final int AV_CH_LAYOUT_QUAD = 0x33;
  private static final int AV_SAMPLE_FMT_S16 = 1;
  private static final int AV_SAMPLE_FMT_S32 = 2;
  private static final int AV_SAMPLE_FMT_FLT = 3;
  private static final int AV_SAMPLE_FMT_FLTP = 8;
  private static final int AV_ROUND_UP = 3;
  private static final int SWS_BICUBIC = 4;
  private static final int AVFMT_GLOBALHEADER = 0x40;
  private static final int CODEC_FLAG_GLOBAL_HEADER = 0x400000;
  private static final int AV_PIX_FMT_NONE = -1;
  private static final int AV_PIX_FMT_YUV420P = 0;
  private static final int AV_PIX_FMT_RGB24 = 2;
  private static final int AV_PIX_FMT_YUV444P = 5;
  private static final int AV_PIX_FMT_BGRA = 30;
  private static final long AV_TIME_BASE = 1000000;
  private static final long AV_TIME_BASE_PARTIAL = 250000;

  public static final int SEEK_SET = 0;
  public static final int SEEK_CUR = 1;
  public static final int SEEK_END = 2;

  //for use with VideoDecoder
  public static final int AV_CODEC_ID_NONE = 0;
  public static final int AV_CODEC_ID_MPEG1VIDEO = 1;
  public static final int AV_CODEC_ID_MPEG2VIDEO = 2;
  public static final int AV_CODEC_ID_H263 = 5;
  public static final int AV_CODEC_ID_MPEG4 = 13;
  public static final int AV_CODEC_ID_H264 = 28;
  public static final int AV_CODEC_ID_THEORA = 31;
  public static final int AV_CODEC_ID_VP8 = 141;

  //a few audio codecs
  public static final int AV_CODEC_ID_PCM_S16LE = 0x10000;  //wav file
  public static final int AV_CODEC_ID_FLAC = 0x1500c;

  //returned by Decoder.read()
  public static final int END_FRAME = -1;
  public static final int NULL_FRAME = 0;  //could be metadata frame
  public static final int AUDIO_FRAME = 1;
  public static final int VIDEO_FRAME = 2;

  private static interface AVCodecLibrary extends Library {
    void avcodec_register_all();
    AVCodec avcodec_find_decoder(int codec_id);
    int avcodec_decode_video2(AVCodecContext avctx,AVFrame picture,IntByReference got_picture_ptr,AVPacket avpkt);
    int avcodec_decode_audio4(AVCodecContext avctx,AVFrame frame,IntByReference got_frame_ptr,AVPacket avpkt);
    int avcodec_open2(AVCodecContext avctx,AVCodec codec,Pointer options);
    AVCodecContext avcodec_alloc_context3(AVCodec codec);
    AVFrame avcodec_alloc_frame();
    void avcodec_free_frame(PointerByReference frame);
    void av_init_packet(AVPacket pkt);
    void av_free_packet(AVPacket pkt);
    //encoding
    AVCodec avcodec_find_encoder(int codec_id);
    int avpicture_alloc(AVPicture pic, int pix_fmt, int width, int height);
    int avpicture_free(AVPicture pic);
    int avcodec_encode_video2(AVCodecContext cc, AVPacket pkt, AVFrame frame, IntByReference intref);
    int avcodec_encode_audio2(AVCodecContext cc, AVPacket pkt, AVFrame frame, IntByReference intref);
    int avcodec_fill_audio_frame(AVFrame frame, int nb_channels, int fmt, Pointer buf, int bufsize, int align);
    int avcodec_close(AVCodecContext cc);
  }
  private static interface AVDeviceLibrary extends Library {
    void avdevice_register_all();
  }
  private static interface AVFilterLibrary extends Library {
    void avfilter_register_all();
  }
  private static interface AVFormatLibrary extends Library {
    void av_register_all();
    void av_register_output_format(AVOutputFormat oformat);
    AVOutputFormat av_guess_format(String shortName, String fileName, String mimeType);
    int av_find_best_stream(AVFormatContext ic,int type,int wanted_stream_nb,int related_stream
      ,PointerByReference decoder_ret, int flags);
    Pointer avio_alloc_context(Pointer buffer,int buffer_size,int write_flag,Pointer opaque
      ,Callback read,Callback write,Callback seek);
    Pointer avformat_alloc_context();
    int avio_close(Pointer ctx);
    void avformat_free_context(AVFormatContext s);
    int avformat_open_input(PointerByReference ps,String filename,Pointer fmt,Pointer options);
    int avformat_find_stream_info(AVFormatContext ic,PointerByReference options);
    int av_read_frame(AVFormatContext s,AVPacket pkt);
    Pointer av_find_input_format(String name);
    Pointer av_iformat_next(Pointer ptr);
    int avformat_seek_file(AVFormatContext ctx, int stream_idx, long min_ts, long ts, long max_ts, int flags);
    int av_seek_frame(AVFormatContext ctx, int stream_idx, long ts, int flags);
    //encoding
//    int avformat_alloc_output_context2(PointerByReference ref, AVOutputFormat of, String format, String filename);  //n/a in libav.org
    AVStream avformat_new_stream(AVFormatContext fc, AVCodec codec);
    int avformat_write_header(AVFormatContext fc, Pointer opts);
    int av_interleaved_write_frame(AVFormatContext fc, AVPacket pkt);
    int av_write_frame(AVFormatContext fc, AVPacket pkt);
    int avio_flush(Pointer io_ctx);
    void av_dump_format(AVFormatContext fmt_ctx, int index, String url, int is_output);
    int av_write_trailer(AVFormatContext fc);
  }
  private static interface AVUtilLibrary extends Library {
    void av_image_copy(Pointer dst_data[],int dst_linesizes[]
      , Pointer src_data[],int src_linesizes[],int pix_fmt,int width,int height);
    int av_get_bytes_per_sample(int sample_fmt);
    Pointer av_malloc(int size);
    void av_free(Pointer ptr);
    int av_image_alloc(Pointer pointers[],int linesizes[],int w,int h,int pix_fmt,int align);
    int av_opt_set(Pointer obj,String name,String val,int search_flags);
    int av_opt_set_int(Pointer obj,String name,long val,int search_flags);
//    int av_opt_set_sample_fmt(Pointer obj,String name,int fmt,int search_flags);  //n/a in libav.org
    int av_opt_get(Pointer obj,String name,int search_flags,Pointer val[]);
    int av_opt_get_int(Pointer obj,String name,int search_flags,long val[]);
//    int av_opt_get_pixel_fmt(Pointer obj,String name,int search_flags,int val[]);
    Pointer av_opt_find(Pointer obj, String name, String unit, int opt_flags, int search_flags);
    Pointer av_opt_next(Pointer obj, Pointer prev);
    Pointer av_opt_child_next(Pointer obj, Pointer prev);
    long av_rescale_rnd(long a,long b,long c,int AVRounding);
    int av_samples_alloc(Pointer audio_data[],int linesize[],int nb_channels,int nb_samples,int sample_fmt
      ,int align);
    long av_rescale_q(long a, AVRational b, AVRational c);
    int av_samples_get_buffer_size(Pointer linesize, int chs, int samples, int sample_fmt, int align);
    int av_log_set_level(int lvl);
    Pointer av_dict_get(Pointer dict, String key, Pointer prev, int flags);
    int av_dict_set(PointerByReference dictref, String key, String value, int flags);
  }
  private static interface SWResampleLibrary extends Library {
    Pointer swr_alloc();
    int swr_init(Pointer ctx);
    long swr_get_delay(Pointer ctx,long base);
    int swr_convert(Pointer ctx,Pointer out_arg[],int out_count,Pointer in_arg[],int in_count);
    void swr_free(PointerByReference ctx);
  }
  private static interface AVResampleLibrary extends Library {
    Pointer avresample_alloc_context();
    int avresample_open(Pointer ctx);
    int avresample_free(Pointer ctx);
    long avresample_get_delay(Pointer ctx);
    int avresample_convert(Pointer ctx,Pointer out_arg[],int out_plane_size, int out_count,Pointer in_arg[]
      , int in_plane_size, int in_count);
    void avresample_free(PointerByReference ctx);
  }
  private static interface SWScaleLibrary extends Library {
    Pointer sws_getContext(int srcW,int srcH,int srcFormat,int dstW,int dstH,int dstFormat
      ,int flags,Pointer srcFilter,Pointer dstFilter, Pointer param);
    int sws_scale(Pointer c, Pointer srcSlice[],int srcStride[],int srcSliceY,int srcSliceH
      ,Pointer dst[],int dstStride[]);
    void sws_freeContext(Pointer ctx);
  }
  static private AVCodecLibrary avcodec;
  static private AVDeviceLibrary avdevice;
  static private AVFilterLibrary avfilter;
  static private AVFormatLibrary avformat;
  static private AVUtilLibrary avutil;
  static private AVResampleLibrary avresample;  //libav.org
  static private SWResampleLibrary swresample;  //ffmpeg.org
  static private SWScaleLibrary swscale;
  static private int ffiobufsiz = 32 * 1024;

  //helper functions

  private static int av_opt_get_int(Pointer obj, String name) {
    long tmp[] = new long[1];
    avutil.av_opt_get_int(obj, name, 0, tmp);
    return (int)tmp[0];
  }

  private static Pointer av_opt_get(Pointer obj, String name) {
    Pointer tmp[] = new Pointer[1];
    avutil.av_opt_get(obj, name, 0, tmp);
    return tmp[0];
  }

  private static int av_opt_get_pixel_fmt(Pointer obj, String name) {
//    int tmp[] = new int[1];
//    avutil.av_opt_get_pixel_fmt(obj, name, 0, tmp);
    long tmp[] = new long[1];
    avutil.av_opt_get_int(obj, name, 0, tmp);
    return (int)tmp[0];
  }

  //these are ffmpeg/2.0 API versions (Windows)
  public static String avcodecAPI[] = {"", "-55", "-54"};
  public static String avdeviceAPI[] = {"", "-55", "-54"};
  public static String avfilterAPI[] = {"", "-3"};
  public static String avformatAPI[] = {"", "-55", "-54"};
  public static String avutilAPI[] = {"", "-52"};
  //postproc-52
  public static String swresampleAPI[] = {"", "-1", "-0"};
  public static String swscaleAPI[] = {"", "-2"};

  private static boolean libav_org;

  private static Object load(String name, String vers[], Class cls) {
    for(int a=0;a<vers.length;a++) {
      try {
        return Native.loadLibrary(name + vers[a], cls);
      } catch (Throwable t) {}
    }
    return null;
  }

  /**
   * Initialize FFMPEG.  Must call before calling any other function.
   */
  public static boolean init() {
    if (avcodec != null) return true;
    try {
      avcodec = (AVCodecLibrary) load("avcodec", avcodecAPI, AVCodecLibrary.class);
      if (avcodec == null) throw new Exception("avcodec not found");
      avdevice = (AVDeviceLibrary) load("avdevice", avdeviceAPI, AVDeviceLibrary.class);
      if (avdevice == null) throw new Exception("avdevice not found");
      avfilter = (AVFilterLibrary) load("avfilter", avfilterAPI, AVFilterLibrary.class);
      if (avfilter == null) throw new Exception("avfilter not found");
      avformat = (AVFormatLibrary) load("avformat", avformatAPI, AVFormatLibrary.class);
      if (avformat == null) throw new Exception("avformat not found");
      avutil = (AVUtilLibrary) load("avutil", avutilAPI, AVUtilLibrary.class);
      if (avutil == null) throw new Exception("avutil not found");
      swresample = (SWResampleLibrary) load("swresample", swresampleAPI, SWResampleLibrary.class);
      if (swresample == null) {
        avresample = (AVResampleLibrary) load("avresample", swresampleAPI, AVResampleLibrary.class);
        if (avresample == null) throw new Exception("resample not found");
        libav_org = true;
      } else {
        libav_org = false;
      }
      swscale = (SWScaleLibrary) load("swscale", swscaleAPI, SWScaleLibrary.class);
      if (swscale == null) throw new Exception("swscale not found");

      avcodec.avcodec_register_all();
      avdevice.avdevice_register_all();
      avfilter.avfilter_register_all();
      avformat.av_register_all();
      register_vpx();
    } catch (Throwable t) {
      JFLog.log("CodecPack:" + t);
      return false;
    }
    return true;
  }

  //ffmpeg has not created a mux for "raw vpx" yet (and they decided not to)
  //see https://trac.ffmpeg.org/ticket/3183
  private static AVOutputFormat vpx;
  private static void register_vpx() {
    vpx = avformat.av_guess_format("vpx", null, null);
    if (vpx != null) return;
    //basically just clone h264
    AVOutputFormat h264 = avformat.av_guess_format("h264", null, null);
    h264.read();
    if (h264 == null) {
      JFLog.log("FFMPEG:Unable to register vpx codec");
      return;
    }
    vpx = new AVOutputFormat();
    vpx.name = "vpx";
    vpx.long_name = "raw vpx";
    vpx.extensions = "vpx";
    vpx.audio_codec = AV_CODEC_ID_NONE;
    vpx.video_codec = AV_CODEC_ID_VP8;
    vpx.write_packet = h264.write_packet;
    vpx.flags = h264.flags;
    vpx.write();
    avformat.av_register_output_format(vpx);
  }

  //libav.org does not provide this function : easy to implement
  private static AVFormatContext avformat_alloc_output_context2(String codec) {
    AVFormatContext ctx = new AVFormatContext(avformat.avformat_alloc_context());
    ctx.read();
    ctx.oformat = avformat.av_guess_format(codec, null, null).getPointer();
    ctx.write();
    return ctx;
  }

  /** For testing purposes */
  public static void list_input_formats() {
    AVInputFormat fmt;
    Pointer ptr = null;
    while (true) {
      ptr = avformat.av_iformat_next(ptr);
      if (ptr == null) break;
      fmt = new AVInputFormat(ptr);
      fmt.read();
      System.out.println("fmt=" + fmt.name);
    }
  }

  /*
   * Base class for decoder/encoder.
   */
  public static class Coder {}

  public static interface read_callback extends Callback {
    public int read(Pointer o, Pointer buffer, int size);
  }
  public static interface write_callback extends Callback {
    public int write(Pointer o, Pointer buffer, int size);
  }
  public static interface seek_callback extends Callback {
    public long seek(Pointer o, long pos, int how);
  }

  /*
   * Handles demux and decoding.
   */
  public static class Decoder extends Coder {
    private Pointer io_ctx;
    private AVFormatContext fmt_ctx;
    private Pointer ff_buffer;
    private byte java_buffer[] = new byte[ffiobufsiz];
    private FFMPEGIO io;
    private Pointer input_fmt;

    private int video_stream_idx;
    private AVStream video_stream;
    private AVCodecContext video_codec_ctx;
    private Pointer sws_ctx;
    //compressed video
    private int video_dst_bufsize;
    private Pointer video_dst_data[] = new Pointer[4];
    private int video_dst_linesize[] = new int[4];
    //rgb video
    private int rgb_video_dst_bufsize;
    private Pointer rgb_video_dst_data[] = new Pointer[4];
    private int rgb_video_dst_linesize[] = new int[4];

    private int audio_stream_idx;
    private AVStream audio_stream;
    private AVCodecContext audio_codec_ctx;
    private Pointer swr_ctx;

    //compressed audio
    private int src_rate;
    //user audio
    private int dst_rate;
    private int dst_nb_channels;
    private int dst_sample_fmt;
    private Pointer audio_dst_data[] = new Pointer[4];
    private int audio_dst_linesize[] = new int[4];

    private AVPacket pkt;
    private int pkt_size_left;
    private boolean pkt_key_frame;

    private AVFrame frame;

    private int video[];
    private short audio[];

    public read_callback read = new read_callback() {
      public int read(Pointer o, Pointer buffer, int size) {
        if (size > ffiobufsiz) size = ffiobufsiz;  //this happens sometimes - why?
        int read = io.read(Decoder.this, java_buffer, size);
        if (read <= 0) return 0;
        buffer.write(0, java_buffer, 0, read);
        return read;
      }
    };
    public write_callback write = new write_callback() {
      public int write(Pointer o, Pointer buffer, int size) {
        return io.write(Decoder.this, buffer.getByteArray(0, size));
      }
    };
    public seek_callback seek = new seek_callback() {
      public long seek(Pointer o, long pos, int how) {
        if (how == 0x10000) {
          //AVSEEK_SIZE - return size of file
          long curpos = io.seek(Decoder.this, 0, SEEK_CUR);
          long filesize = io.seek(Decoder.this, 0, SEEK_END);
          io.seek(Decoder.this, curpos, SEEK_SET);
          return filesize;
        }
        return io.seek(Decoder.this, pos, how);
      }
    };

    //returns stream idx >= 0
    int open_codec_context(AVFormatContext fmt_ctx, int type)
    {
      int ret;
      int stream_idx;
      AVStream stream;
      AVCodecContext codec_ctx;
      AVCodec codec;
      stream_idx = avformat.av_find_best_stream(fmt_ctx, type, -1, -1, null, 0);
      if (stream_idx < 0) {
        return stream_idx;
      } else {
        stream = new AVStream(fmt_ctx.streams.getPointerArray(0,stream_idx+1)[stream_idx]);
        stream.read();
        codec_ctx = new AVCodecContext(stream.codec);
        codec_ctx.read();
        codec = avcodec.avcodec_find_decoder(codec_ctx.codec_id);
        if (codec == null) {
          return -1;
        }
        if ((ret = avcodec.avcodec_open2(codec_ctx, codec, null)) < 0) {
          return ret;
        }
      }
      return stream_idx;
    }

    /* Starts demuxing/decoding a stream.
     * @param new_width - scale video to new width (-1 = use stream width)
     * @param new_height - scale video to new height (-1 = use stream height)
     * @param new_chs - # of channels to mix to (-1 = use stream channels)
     * @param new_freq - output sampling rate (-1 = use stream rate)
     * @param seekable - can you seek input? (true=file false=stream)
     * NOTE : Audio output is always 16bit
     */
    public boolean start(FFMPEGIO io, int new_width, int new_height, int new_chs, int new_freq, boolean seekable) {
      JFLog.log("Decoder.start");
      int res;
      this.io = io;
      ff_buffer = avutil.av_malloc(ffiobufsiz);
      io_ctx = avformat.avio_alloc_context(ff_buffer, ffiobufsiz, 0, null, read, write, seekable ? seek : null);
      fmt_ctx = new AVFormatContext(avformat.avformat_alloc_context());
      fmt_ctx.read();
      fmt_ctx.pb = io_ctx;
      fmt_ctx.writeField("pb");
      PointerByReference fmt_ctx_ref = new PointerByReference(fmt_ctx.getPointer());
      if ((res = avformat.avformat_open_input(fmt_ctx_ref, "stream", null, null)) != 0) {
        JFLog.log("avformat_open_input() failed : " + res);
        return false;
      }
      fmt_ctx.read();
      if ((res = avformat.avformat_find_stream_info(fmt_ctx, null)) < 0) {
        JFLog.log("avformat_find_stream_info() failed : " + res);
        return false;
      }
      avformat.av_dump_format(fmt_ctx, 0, "memory_io", 0);
      return open_codecs(new_width, new_height, new_chs, new_freq);
    }

    /**
     * Alternative start that works with files.
     *
     * Example: start("/dev/video0", "v4l2", ...);
     *
     * NOTE:input_format may be null
     */

    public boolean start(String file, String input_format, int new_width, int new_height, int chs, int new_freq) {
      JFLog.log("Decoder.start");
      int res;
      fmt_ctx = new AVFormatContext(avformat.avformat_alloc_context());
      fmt_ctx.read();
      if (input_format != null) {
        input_fmt = avformat.av_find_input_format(input_format);
        if (input_fmt == null) {
          JFLog.log("FFMPEG:av_find_input_format failed:" + input_format);
          return false;
        }
      }
      PointerByReference fmt_ctx_ref = new PointerByReference(fmt_ctx.getPointer());
      if ((res = avformat.avformat_open_input(fmt_ctx_ref, file, input_fmt, null)) != 0) {
        JFLog.log("avformat_open_input() failed : " + res);
        return false;
      }
      fmt_ctx.read();

      avformat.av_dump_format(fmt_ctx, 0, "memory_io", 0);

      if ((res = avformat.avformat_find_stream_info(fmt_ctx, null)) < 0) {
        JFLog.log("avformat_find_stream_info() failed : " + res);
        return false;
      }
      return open_codecs(new_width, new_height, chs, new_freq);
    }

    private boolean open_codecs(int new_width, int new_height, int new_chs, int new_freq) {
      if ((video_stream_idx = open_codec_context(fmt_ctx, AVMEDIA_TYPE_VIDEO)) >= 0) {
        video_stream = new AVStream(fmt_ctx.streams.getPointerArray(0,video_stream_idx+1)[video_stream_idx]);
        video_stream.read();
        video_codec_ctx = new AVCodecContext(video_stream.codec);
        video_codec_ctx.read();
        if (new_width == -1) new_width = video_codec_ctx.width;
        if (new_height == -1) new_height = video_codec_ctx.height;
        if ((video_dst_bufsize = avutil.av_image_alloc(video_dst_data, video_dst_linesize
          , video_codec_ctx.width, video_codec_ctx.height
          , video_codec_ctx.pix_fmt, 1)) < 0) return false;
        if ((rgb_video_dst_bufsize = avutil.av_image_alloc(rgb_video_dst_data, rgb_video_dst_linesize
          , new_width, new_height
          , AV_PIX_FMT_BGRA, 1)) < 0) return false;
        int px = rgb_video_dst_bufsize / 4;
        video = new int[px];
        //create video conversion context
        sws_ctx = swscale.sws_getContext(video_codec_ctx.width, video_codec_ctx.height, video_codec_ctx.pix_fmt
          , new_width, new_height, AV_PIX_FMT_BGRA
          , SWS_BILINEAR, null, null, null);
      }

      if ((audio_stream_idx = open_codec_context(fmt_ctx, AVMEDIA_TYPE_AUDIO)) >= 0) {
        audio_stream = new AVStream(fmt_ctx.streams.getPointerArray(0,audio_stream_idx+1)[audio_stream_idx]);
        audio_stream.read();
        audio_codec_ctx = new AVCodecContext(audio_stream.codec);
        audio_codec_ctx.read();
        //create audio conversion context
        if (libav_org)
          swr_ctx = avresample.avresample_alloc_context();
        else
          swr_ctx = swresample.swr_alloc();
        if (new_chs == -1) new_chs = audio_codec_ctx.channels;
        long new_layout;
        switch (new_chs) {
          case 1: new_layout = AV_CH_LAYOUT_MONO; dst_nb_channels = 1; break;
          case 2: new_layout = AV_CH_LAYOUT_STEREO; dst_nb_channels = 2; break;
          case 4: new_layout = AV_CH_LAYOUT_QUAD; dst_nb_channels = 4; break;
          default: return false;
        }
        long src_layout = audio_codec_ctx.channel_layout;
        if (src_layout == 0) {
          switch (audio_codec_ctx.channels) {
            case 1: src_layout = AV_CH_LAYOUT_MONO; break;
            case 2: src_layout = AV_CH_LAYOUT_STEREO; break;
            case 4: src_layout = AV_CH_LAYOUT_QUAD; break;
            default: return false;
          }
        }
        dst_sample_fmt = AV_SAMPLE_FMT_S16;
        src_rate = audio_codec_ctx.sample_rate;
        if (new_freq == -1) new_freq = src_rate;
        avutil.av_opt_set_int(swr_ctx, "in_channel_layout",     src_layout, 0);
        avutil.av_opt_set_int(swr_ctx, "in_sample_rate",        src_rate, 0);
//        avutil.av_opt_set_sample_fmt(swr_ctx, "in_sample_fmt",  audio_codec_ctx.sample_fmt, 0);
        avutil.av_opt_set_int(swr_ctx, "in_sample_fmt",  audio_codec_ctx.sample_fmt, 0);
        avutil.av_opt_set_int(swr_ctx, "out_channel_layout",    new_layout, 0);
        avutil.av_opt_set_int(swr_ctx, "out_sample_rate",       new_freq, 0);
//        avutil.av_opt_set_sample_fmt(swr_ctx, "out_sample_fmt", dst_sample_fmt, 0);
        avutil.av_opt_set_int(swr_ctx, "out_sample_fmt", dst_sample_fmt, 0);
        if (libav_org)
          avresample.avresample_open(swr_ctx);
        else
          swresample.swr_init(swr_ctx);
        dst_rate = new_freq;
      }

      if ((frame = avcodec.avcodec_alloc_frame()) == null) return false;
      frame.read();
      pkt = new AVPacket();
      avcodec.av_init_packet(pkt);
      pkt.read();
      pkt.data = null;
      pkt.size = 0;
      pkt.write();
      pkt_size_left = 0;

      return true;
    }

    /** Stop decoder. Frees all resources */
    public void stop() {
      JFLog.log("Decoder.stop");
      if (io_ctx != null) {
        avformat.avio_close(io_ctx);
        io_ctx = null;
        ff_buffer = null;
      }
      if (fmt_ctx != null) {
        avformat.avformat_free_context(fmt_ctx);
        fmt_ctx = null;
        //BUG:I think this frees all the codec stuff too ???
      }
      if (frame != null) {
        PointerByReference ref = new PointerByReference();
        ref.setValue(frame.getPointer());
        avcodec.avcodec_free_frame(ref);
        frame = null;
      }
      if (video_dst_data[0] != null) {
        avutil.av_free(video_dst_data[0]);
        video_dst_data[0] = null;
      }
      if (rgb_video_dst_data[0] != null) {
        avutil.av_free(rgb_video_dst_data[0]);
        rgb_video_dst_data[0] = null;
      }
      if (sws_ctx != null) {
        swscale.sws_freeContext(sws_ctx);
        sws_ctx = null;
      }
      if (swr_ctx != null) {
        if (libav_org)
          avresample.avresample_free(new PointerByReference(swr_ctx));
        else
          swresample.swr_free(new PointerByReference(swr_ctx));
        swr_ctx = null;
      }
    }

    /** Reads next frame in stream and returns what type it was : AUDIO_FRAME, VIDEO_FRAME, NULL_FRAME or END_FRAME */
    public int read()
    {
      //read another frame
      if (pkt_size_left == 0) {
        if (avformat.av_read_frame(fmt_ctx, pkt) >= 0) {
          pkt_size_left = pkt.size;
          pkt_key_frame = ((pkt.flags & 0x0001) == 0x0001);
        } else {
          return END_FRAME;
        }
      }

      //try to decode another frame
      if (pkt.stream_index == video_stream_idx) {
        //extract a video frame
        IntByReference got_frame = new IntByReference();
        if (avcodec.avcodec_decode_video2(video_codec_ctx, frame, got_frame, pkt) < 0) {
          pkt_size_left = 0;
          return NULL_FRAME;
        }
        pkt_size_left = 0;  //use entire packet
        if (got_frame.getValue() == 0) return NULL_FRAME;
        avcodec.av_free_packet(pkt);
        avutil.av_image_copy(video_dst_data, video_dst_linesize
          , frame.data, frame.linesize
          , video_codec_ctx.pix_fmt, video_codec_ctx.width, video_codec_ctx.height);
        //convert image to RGBA format
        swscale.sws_scale(sws_ctx, video_dst_data, video_dst_linesize, 0, video_codec_ctx.height
          , rgb_video_dst_data, rgb_video_dst_linesize);
        //now copy to memory block
        rgb_video_dst_data[0].read(0, video, 0, video.length);
        return VIDEO_FRAME;
      }

      if (pkt.stream_index == audio_stream_idx) {
        //extract an audio frame
        IntByReference got_frame = new IntByReference();
        int ret = avcodec.avcodec_decode_audio4(audio_codec_ctx, frame, got_frame, pkt);
        if (ret < 0) {
          pkt_size_left = 0;
          return NULL_FRAME;
        }
        ret = Math.min(pkt_size_left, ret);
        pkt_size_left -= ret;
        if (pkt_size_left == 0) {
          avcodec.av_free_packet(pkt);
        }
        if (got_frame.getValue() == 0) {
          return NULL_FRAME;
        }
//        int unpadded_linesize = frame.nb_samples * avutil.av_get_bytes_per_sample(audio_codec_ctx.sample_fmt);
        //convert to new format
        int dst_nb_samples;
        if (libav_org) {
          dst_nb_samples = (int)avutil.av_rescale_rnd(avresample.avresample_get_delay(swr_ctx)
            + frame.nb_samples, dst_rate, src_rate, AV_ROUND_UP);
        } else {
          dst_nb_samples = (int)avutil.av_rescale_rnd(swresample.swr_get_delay(swr_ctx, src_rate)
            + frame.nb_samples, dst_rate, src_rate, AV_ROUND_UP);
        }
        if ((avutil.av_samples_alloc(audio_dst_data, audio_dst_linesize, dst_nb_channels
          , dst_nb_samples, dst_sample_fmt, 1)) < 0) return NULL_FRAME;
        if (libav_org) {
          avresample.avresample_convert(swr_ctx, audio_dst_data, 0, dst_nb_samples
            , frame.extended_data.getPointerArray(0,8), 0, frame.nb_samples);
        } else {
          swresample.swr_convert(swr_ctx, audio_dst_data, dst_nb_samples
            , frame.extended_data.getPointerArray(0,8), frame.nb_samples);
        }
        int count = dst_nb_samples * dst_nb_channels;
        if (audio == null || audio.length != count) {
          audio = new short[count];
        }
        audio_dst_data[0].read(0, audio, 0, count);
        //free audio_dst_data
        if (audio_dst_data[0] != null) {
          avutil.av_free(audio_dst_data[0]);
          audio_dst_data[0] = null;
        }
        return AUDIO_FRAME;
      }
      return NULL_FRAME;
    }

    public int[] get_video() {
      return video;
    }

    public short[] get_audio() {
      return audio;
    }

    public int getWidth() {
      if (video_codec_ctx == null) return 0;
      return video_codec_ctx.width;
    }

    public int getHeight() {
      if (video_codec_ctx == null) return 0;
      return video_codec_ctx.height;
    }

    public float getFrameRate() {
      if (video_codec_ctx == null) return 0;
      return video_codec_ctx.time_base_den / video_codec_ctx.time_base_num / video_codec_ctx.ticks_per_frame;
    }

    /** Returns duration in seconds. */
    public long getDuration() {
      if (fmt_ctx == null) return 0;
      if (fmt_ctx.duration << 1 == 0) return 0;  //0x8000000000000000
      return fmt_ctx.duration / AV_TIME_BASE;  //return in seconds
    }

    public int getSampleRate() {
      if (audio_codec_ctx == null) return 0;
      return audio_codec_ctx.sample_rate;
    }

    public int getChannels() {
      return dst_nb_channels;
    }

    public int getBitsPerSample() {
      return 16;  //output is always converted to 16bits/sample (signed)
    }

    public boolean seek(long seconds) {
      //AV_TIME_BASE is 1000000fps
      seconds *= AV_TIME_BASE;
/*      int ret = avformat.avformat_seek_file(fmt_ctx, -1
        , seconds - AV_TIME_BASE_PARTIAL, seconds, seconds + AV_TIME_BASE_PARTIAL, 0);*/
      int ret = avformat.av_seek_frame(fmt_ctx, -1, seconds, 0);
      if (ret < 0) JFLog.log("av_seek_frame:" + ret);
      return ret >= 0;
    }

    public int getVideoBitRate() {
      if (video_codec_ctx == null) return 0;
      return video_codec_ctx.bit_rate;
    }

    public int getAudioBitRate() {
      if (audio_codec_ctx == null) return 0;
      return audio_codec_ctx.bit_rate;
    }

    /** Indicates if last frame decoded was a key frame */
    public boolean isKeyFrame() {
      return pkt_key_frame;
    }

    /** Resizes video output */
    public boolean resize(int new_width, int new_height) {
      if (video_stream == null) return false;  //no video

      if (rgb_video_dst_data[0] != null) {
        avutil.av_free(rgb_video_dst_data[0]);
        rgb_video_dst_data[0] = null;
      }

      if ((rgb_video_dst_bufsize = avutil.av_image_alloc(rgb_video_dst_data, rgb_video_dst_linesize
        , new_width, new_height
        , AV_PIX_FMT_BGRA, 1)) < 0) return false;

      video = new int[rgb_video_dst_bufsize / 4];

      if (sws_ctx != null) {
        swscale.sws_freeContext(sws_ctx);
        sws_ctx = null;
      }

      sws_ctx = swscale.sws_getContext(video_codec_ctx.width, video_codec_ctx.height, video_codec_ctx.pix_fmt
        , new_width, new_height, AV_PIX_FMT_BGRA
        , SWS_BILINEAR, null, null, null);

      return true;
    }
  }

  /*
   * Handles decoding a raw video stream.
   */
  public static class VideoDecoder {
    private AVCodecContext video_codec_ctx;
    private AVCodec video_codec;
    private Pointer sws_ctx;
    //compressed video
    private int video_dst_bufsize;
    private Pointer video_dst_data[] = new Pointer[4];
    private int video_dst_linesize[] = new int[4];
    //rgb video
    private int rgb_video_dst_bufsize;
    private Pointer rgb_video_dst_data[] = new Pointer[4];
    private int rgb_video_dst_linesize[] = new int[4];

    private AVPacket pkt;
    private int pkt_size_left;

    private AVFrame frame;

    private int video[];

    /* Starts decoding a raw video stream.
     * @param new_width - scale video to new width (-1 = invalid)
     * @param new_height - scale video to new height (-1 = invalid)
     */
    public boolean start(int codec_id, int new_width, int new_height) {
      JFLog.log("VideoDecoder.start");
      video_codec = avcodec.avcodec_find_decoder(codec_id);
      if (video_codec == null) {
        return false;
      }
      video_codec_ctx = avcodec.avcodec_alloc_context3(video_codec);
      video_codec_ctx.read();

      //set default values
      video_codec_ctx.codec_id = codec_id;
      video_codec_ctx.width = new_width;
      video_codec_ctx.height = new_height;
      video_codec_ctx.pix_fmt = AV_PIX_FMT_YUV420P;
      video_codec_ctx.write();

      if ((avcodec.avcodec_open2(video_codec_ctx, video_codec, null)) < 0) {
        return false;
      }

      //BUG : what are default values???
      if (new_width == -1) new_width = video_codec_ctx.width;
      if (new_height == -1) new_height = video_codec_ctx.height;
      if ((video_dst_bufsize = avutil.av_image_alloc(video_dst_data, video_dst_linesize
        , video_codec_ctx.width, video_codec_ctx.height
        , video_codec_ctx.pix_fmt, 1)) < 0) return false;
      if ((rgb_video_dst_bufsize = avutil.av_image_alloc(rgb_video_dst_data, rgb_video_dst_linesize
        , new_width, new_height
        , AV_PIX_FMT_BGRA, 1)) < 0) return false;
      //create video conversion context
      sws_ctx = swscale.sws_getContext(video_codec_ctx.width, video_codec_ctx.height, video_codec_ctx.pix_fmt
        , new_width, new_height, AV_PIX_FMT_BGRA
        , SWS_BILINEAR, null, null, null);

      if ((frame = avcodec.avcodec_alloc_frame()) == null) return false;
      frame.read();
      pkt = new AVPacket();
      avcodec.av_init_packet(pkt);
      pkt.read();
      pkt.data = null;
      pkt.size = 0;
      pkt.write();
      pkt_size_left = 0;

      return true;
    }

    /** Stop video decoder.  Frees all resources */
    public void stop() {
      JFLog.log("VideoDecoder.stop");
      if (frame != null) {
        PointerByReference ref = new PointerByReference();
        ref.setValue(frame.getPointer());
        avcodec.avcodec_free_frame(ref);
        frame = null;
      }
      if (pkt != null) {
        if (pkt.data != null) {
          avutil.av_free(pkt.data);
          pkt.data = null;
          pkt.write();
        }
      }
      if (video_codec_ctx != null) {
        avcodec.avcodec_close(video_codec_ctx);
        avutil.av_free(video_codec_ctx.getPointer());
        video_codec_ctx = null;
      }
      if (video_dst_data[0] != null) {
        avutil.av_free(video_dst_data[0]);
        video_dst_data[0] = null;
      }
      if (rgb_video_dst_data[0] != null) {
        avutil.av_free(rgb_video_dst_data[0]);
        rgb_video_dst_data[0] = null;
      }
      if (sws_ctx != null) {
        swscale.sws_freeContext(sws_ctx);
        sws_ctx = null;
      }
    }

    public int[] decode(byte data[])
    {
      video = null;
      //read another frame
      if (pkt_size_left == 0) {
        if (pkt.data != null) {
          avutil.av_free(pkt.data);
          pkt.data = null;
        }
        pkt.data = avutil.av_malloc(data.length);
        pkt.data.write(0, data, 0, data.length);
        pkt.size = data.length;
        pkt.write();
        pkt_size_left = pkt.size;
      }

      //extract a video frame
      IntByReference got_frame = new IntByReference();
      if (avcodec.avcodec_decode_video2(video_codec_ctx, frame, got_frame, pkt) < 0) {
        pkt_size_left = 0;
        return null;
      }
      if (got_frame.getValue() == 0) return null;
      avutil.av_image_copy(video_dst_data, video_dst_linesize
        , frame.data, frame.linesize
        , video_codec_ctx.pix_fmt, video_codec_ctx.width, video_codec_ctx.height);
      //convert image to RGBA format
      swscale.sws_scale(sws_ctx, video_dst_data, video_dst_linesize, 0, video_codec_ctx.height
        , rgb_video_dst_data, rgb_video_dst_linesize);
      //now copy to memory block
      int px = rgb_video_dst_bufsize / 4;
      video = new int[px];
      rgb_video_dst_data[0].read(0, video, 0, px);
      pkt_size_left = 0;  //use entire packet
      //do NOT free pkt - it's done in next call here or in stop function

      return video;
    }

    public int getWidth() {
      if (video_codec_ctx == null) return 0;
      return video_codec_ctx.width;
    }

    public int getHeight() {
      if (video_codec_ctx == null) return 0;
      return video_codec_ctx.height;
    }

    public float getFrameRate() {
      if (video_codec_ctx == null) return 0;
      return video_codec_ctx.time_base_den / video_codec_ctx.time_base_num / video_codec_ctx.ticks_per_frame;
    }
  }

  /*
   * Handles encoding and muxing.
   */
  public static class Encoder extends Coder {
    private Pointer io_ctx;
    private Pointer ff_buffer;
    private byte java_buffer[] = new byte[ffiobufsiz];
    private AVFormatContext fmt_ctx;
    private AVOutputFormat out_fmt;
    private AVCodecContext audio_ctx, video_ctx;
    private AVStream audio_stream, video_stream;
    private AVCodec audio_codec, video_codec;
    private FFMPEGIO io;
    private int width, height, fps;
    private int chs, freq;
    private AVFrame audio_frame, video_frame;
    private AVPicture src_pic, dst_pic;
    private Pointer sws_ctx;
    private Pointer swr_ctx;
    private Pointer audio_src_data[] = new Pointer[4];
    private Pointer audio_dst_data[] = new Pointer[4];
    private int audio_dst_linesize[] = new int[4];

    /** Number of frames per group of pictures (GOP).
     * Determines how often key frame is generated.
     * Default = 12
     */
    public int config_gop_size = 12;
    /** Video bit rate.
     * Default = 400000
     */
    public int config_video_bit_rate = 400000;
    /** Audio bit rate.
     * Default = 128000
     */
    public int config_audio_bit_rate = 128000;

    /** Returns Object[] {AVStream, AVCodec, AVCodecContext} */
    private Object[] add_stream(int codec_id) {
      AVCodecContext codec_ctx;
      AVStream stream;
      AVCodec codec;

      codec = avcodec.avcodec_find_encoder(codec_id);
      if (codec == null) return null;
      codec.read();
      stream = avformat.avformat_new_stream(fmt_ctx, codec);
      if (stream == null) return null;
      stream.read();
      stream.id = fmt_ctx.nb_streams-1;
      stream.writeField("id");
      codec_ctx = new AVCodecContext(stream.codec);
      codec_ctx.read();
      switch (codec.type) {
        case AVMEDIA_TYPE_AUDIO:
          codec_ctx.codec_id = codec_id;
          codec_ctx.sample_fmt = AV_SAMPLE_FMT_FLTP;
          if (codec_id == AV_CODEC_ID_PCM_S16LE) {
            codec_ctx.sample_fmt = AV_SAMPLE_FMT_S16;
          }
          if (codec_id == AV_CODEC_ID_FLAC) codec_ctx.sample_fmt = AV_SAMPLE_FMT_S16;
          codec_ctx.bit_rate = config_audio_bit_rate;
          codec_ctx.sample_rate = freq;
          codec_ctx.channels = chs;
          codec_ctx.time_base_num = 1;
          codec_ctx.time_base_den = fps;
          switch (chs) {
            case 1: codec_ctx.channel_layout = AV_CH_LAYOUT_MONO; break;
            case 2: codec_ctx.channel_layout = AV_CH_LAYOUT_STEREO; break;
            case 4: codec_ctx.channel_layout = AV_CH_LAYOUT_QUAD; break;
          }
          break;
        case AVMEDIA_TYPE_VIDEO:
          codec_ctx.codec_id = codec_id;
          codec_ctx.bit_rate = config_video_bit_rate;
          codec_ctx.width = width;
          codec_ctx.height = height;
          codec_ctx.time_base_num = 1;
          codec_ctx.time_base_den = fps;
          codec_ctx.gop_size = config_gop_size;
          codec_ctx.pix_fmt = AV_PIX_FMT_YUV420P;
          if (codec_ctx.codec_id == AV_CODEC_ID_MPEG2VIDEO) {
            codec_ctx.max_b_frames = 2;
          }
          if (codec_ctx.codec_id == AV_CODEC_ID_MPEG1VIDEO) {
            codec_ctx.mb_decision = 2;
          }
          if (codec_ctx.codec_id == AV_CODEC_ID_H264) {
            avutil.av_opt_set(codec_ctx.priv_data, "preset", "slow", 0);
          }
          break;
      }

      if ((out_fmt.flags & AVFMT_GLOBALHEADER) != 0) {
        codec_ctx.flags |= CODEC_FLAG_GLOBAL_HEADER;
      }

      codec_ctx.write();
      return new Object[] {stream, codec, codec_ctx};
    }

    private boolean open_video() {
      int ret = avcodec.avcodec_open2(video_ctx, video_codec, null);
      if (ret < 0) return false;
      video_frame = avcodec.avcodec_alloc_frame();
      if (video_frame == null) return false;
      video_frame.read();
      dst_pic = new AVPicture();
      ret = avcodec.avpicture_alloc(dst_pic, video_ctx.pix_fmt, video_ctx.width, video_ctx.height);
      dst_pic.read();  //needed?
      if (ret < 0) return false;
      if (video_ctx.pix_fmt != AV_PIX_FMT_BGRA) {
        src_pic = new AVPicture();
        ret = avcodec.avpicture_alloc(src_pic, AV_PIX_FMT_BGRA, video_ctx.width, video_ctx.height);
        if (ret < 0) return false;
        src_pic.read();  //needed?
        sws_ctx = swscale.sws_getContext(video_ctx.width, video_ctx.height, AV_PIX_FMT_BGRA
          , video_ctx.width, video_ctx.height, video_ctx.pix_fmt, SWS_BICUBIC, null, null, null);
      }
      //copy data/linesize pointers from dst_pic to frame
      video_frame.data = dst_pic.data;
      video_frame.linesize = dst_pic.linesize;
      video_frame.write();
      return true;
    }

    private boolean open_audio() {
      int ret = avcodec.avcodec_open2(audio_ctx, audio_codec, null);
      if (ret < 0) return false;
      audio_frame = avcodec.avcodec_alloc_frame();
      if (audio_frame == null) return false;
      audio_frame.read();
      audio_frame.format = audio_ctx.sample_fmt;
      audio_frame.sample_rate = freq;
      audio_frame.channel_layout = audio_ctx.channel_layout;
      audio_frame.write();
      if (audio_ctx.sample_fmt == AV_SAMPLE_FMT_S16) return true;  //do NOT convert audio
      //create audio conversion (S16 -> FLTP)
      if (libav_org)
        swr_ctx = avresample.avresample_alloc_context();
      else
        swr_ctx = swresample.swr_alloc();
      avutil.av_opt_set_int(swr_ctx, "in_channel_layout",     audio_ctx.channel_layout, 0);
      avutil.av_opt_set_int(swr_ctx, "in_sample_rate",        freq, 0);
//      avutil.av_opt_set_sample_fmt(swr_ctx, "in_sample_fmt",  AV_SAMPLE_FMT_S16, 0);
      avutil.av_opt_set_int(swr_ctx, "in_sample_fmt",  AV_SAMPLE_FMT_S16, 0);
      avutil.av_opt_set_int(swr_ctx, "out_channel_layout",    audio_ctx.channel_layout, 0);
      avutil.av_opt_set_int(swr_ctx, "out_sample_rate",       freq, 0);
//      avutil.av_opt_set_sample_fmt(swr_ctx, "out_sample_fmt", AV_SAMPLE_FMT_FLTP, 0);
      avutil.av_opt_set_int(swr_ctx, "out_sample_fmt", AV_SAMPLE_FMT_FLTP, 0);
      if (libav_org)
        avresample.avresample_open(swr_ctx);
      else
        swresample.swr_init(swr_ctx);
      return true;
    }

    public read_callback read = new read_callback() {
      public int read(Pointer o, Pointer buffer, int size) {
        int read = io.read(Encoder.this, java_buffer, size);
        if (read ==0) return 0;
        buffer.write(0, java_buffer, 0, read);
        return read;
      }
    };
    public write_callback write = new write_callback() {
      public int write(Pointer o, Pointer buffer, int size) {
        return io.write(Encoder.this, buffer.getByteArray(0, size));
      }
    };
    public seek_callback seek = new seek_callback() {
      public long seek(Pointer o, long pos, int how) {
        if (how == 0x10000) {
          //AVSEEK_SIZE - return size of file
          long curpos = io.seek(Encoder.this, 0, SEEK_CUR);
          long filesize = io.seek(Encoder.this, 0, SEEK_END);
          io.seek(Encoder.this, curpos, SEEK_SET);
          return filesize;
        }
        return io.seek(Encoder.this, pos, how);
      }
    };

    /**
     * Starts Encoding/muxing process.
     */

    public boolean start(FFMPEGIO io, int width, int height, int fps, int chs, int freq, String codec, boolean doVideo, boolean doAudio) {
      JFLog.log("Encoder.start");
      if (doVideo && (width <= 0 || height <= 0)) {
        return false;
      }
      if (doAudio && (chs <= 0 || freq <= 0)) {
        return false;
      }
      if (fps <= 0) fps = 24;  //must be valid, even for audio only
      this.io = io;
      this.width = width;
      this.height = height;
      this.fps = fps;
      this.chs = chs;
      this.freq = freq;
      fmt_ctx = avformat_alloc_output_context2(codec);
      fmt_ctx.read();
      JFLog.log("fmt_ctx=" + fmt_ctx);
      ff_buffer = avutil.av_malloc(ffiobufsiz);
      io_ctx = avformat.avio_alloc_context(ff_buffer, ffiobufsiz, 1, null, read, write, seek);
      if (io_ctx == null) return false;
      fmt_ctx.pb = io_ctx;
      fmt_ctx.writeField("pb");
      out_fmt = new AVOutputFormat(fmt_ctx.oformat);
      out_fmt.read();
      if ((out_fmt.video_codec != AV_CODEC_ID_NONE) && doVideo) {
        Object objs[] = add_stream(out_fmt.video_codec);
        video_stream = (AVStream)objs[0];
        video_codec = (AVCodec)objs[1];
        video_ctx = (AVCodecContext)objs[2];
      }
      if ((out_fmt.audio_codec != AV_CODEC_ID_NONE) && doAudio) {
        Object objs[] = add_stream(out_fmt.audio_codec);
        audio_stream = (AVStream)objs[0];
        audio_codec = (AVCodec)objs[1];
        audio_ctx = (AVCodecContext)objs[2];
      }
      if (video_stream != null) {
        if (!open_video()) return false;
      }
      if (audio_stream != null) {
        if (!open_audio()) return false;
      }
      fmt_ctx.read();
      avformat.avformat_write_header(fmt_ctx, null);  //NOTE:This call changes CodecContext data
      if (audio_frame != null) {
        audio_ctx.read();  //avformat_write_header has changed values
        audio_frame.pts = 0;
        audio_frame.write();
      }
      if (video_frame != null) {
        video_ctx.read();  //avformat_write_header has changed values
        video_frame.pts = 0;
        video_frame.write();
      }
      avformat.av_dump_format(fmt_ctx, 0, "memory_io." + codec, 1);
      return true;
    }
    public boolean add_audio(short sams[], int offset, int length) {
      int nb_samples = length / chs;
      int buffer_size = avutil.av_samples_get_buffer_size(null, chs, nb_samples, AV_SAMPLE_FMT_S16, 0);
      Pointer samples_data = avutil.av_malloc(buffer_size);
      samples_data.write(0, sams, offset, length);
      AVPacket pkt = new AVPacket();
      avcodec.av_init_packet(pkt);
      pkt.read();
      pkt.data = null;
      pkt.size = 0;
      pkt.write();

      if (swr_ctx != null) {
        //convert S16 -> FLTP (some codecs do not support S16)
        if ((avutil.av_samples_alloc(audio_dst_data, audio_dst_linesize, chs
          , nb_samples, AV_SAMPLE_FMT_FLTP, 1)) < 0) return false;
        audio_src_data[0] = samples_data;
        if (libav_org)
          avresample.avresample_convert(swr_ctx, audio_dst_data, 0, nb_samples
            , audio_src_data, 0, nb_samples);
        else
          swresample.swr_convert(swr_ctx, audio_dst_data, nb_samples
            , audio_src_data, nb_samples);
      } else {
        audio_dst_data[0] = samples_data;
      }
      audio_frame.nb_samples = nb_samples;
      audio_frame.write();
      buffer_size = avutil.av_samples_get_buffer_size(null, chs, nb_samples, audio_ctx.sample_fmt, 0);
      int res = avcodec.avcodec_fill_audio_frame(audio_frame, chs, audio_ctx.sample_fmt, audio_dst_data[0]
        , buffer_size, 0);
      if (res < 0) {
        System.out.println("avcodec_fill_audio_frame() failed:" + res);
        return false;
      }
      audio_frame.read();
      IntByReference got_frame = new IntByReference();
      int ret = avcodec.avcodec_encode_audio2(audio_ctx, pkt, audio_frame, got_frame);
      if (ret < 0) {
        System.out.println("avcodec_encode_audio2() failed");
        return false;
      }
      if (got_frame.getValue() != 0 && pkt.size > 0) {
        pkt.stream_index = audio_stream.index;
        pkt.writeField("stream_index");
        ret = avformat.av_interleaved_write_frame(fmt_ctx, pkt);
        avcodec.av_free_packet(pkt);
        if (ret < 0) System.out.println("av_interleaved_write_frame() failed");
      }
      avutil.av_free(samples_data);
      if (swr_ctx != null) {
        //free audio_dst_data (only the first pointer)
        if (audio_dst_data[0] != null) {
          avutil.av_free(audio_dst_data[0]);
          audio_dst_data[0] = null;
        }
      }
      audio_frame.pts++;
      audio_frame.writeField("pts");
      return ret == 0;
    }
    private short samsbuf[];
    public boolean add_audio(short sams[]) {
      if (audio_ctx == null) return false;
      //break up sams if size > framesize
      int frame_size = get_audio_framesize() * chs;  //max samples size
      short full[];
      if (samsbuf != null) {
        full = new short[samsbuf.length + sams.length];
        System.arraycopy(samsbuf, 0, full, 0, samsbuf.length);
        System.arraycopy(sams, 0, full, samsbuf.length, sams.length);
        samsbuf = full;
        if (full.length < frame_size) {
          return true;  //not enough for one frame
        }
      } else {
        if (sams.length < frame_size) {
          samsbuf = new short[sams.length];
          System.arraycopy(sams, 0, samsbuf, 0, sams.length);
          return true;  //not enough for one frame
        }
        full = sams;
      }
      int left = full.length;
      int pos = 0;
      while (left >= frame_size && left > 0) {
        int this_size = left;
        if (this_size > frame_size && frame_size > 0) this_size = frame_size;
        if (!add_audio(full, pos, this_size)) {
          JFLog.log("Error:Unable to add audio");
          return false;
        }
        pos += this_size;
        left -= this_size;
      }
      if (left > 0) {
        samsbuf = new short[left];
        System.arraycopy(full, pos, samsbuf, 0, left);
      } else {
        samsbuf = null;
      }
      return true;
    }
    public boolean add_video(int px[]) {
      if (video_ctx == null) return false;
      if (video_ctx.pix_fmt != AV_PIX_FMT_BGRA) {
        src_pic.data[0].write(0, px, 0, px.length);
        swscale.sws_scale(sws_ctx, src_pic.data, src_pic.linesize, 0, video_ctx.height
          , dst_pic.data, dst_pic.linesize);
      } else {
        dst_pic.data[0].write(0, px, 0, px.length);
      }
      AVPacket pkt = new AVPacket();
      avcodec.av_init_packet(pkt);
      pkt.read();
      pkt.data = null;
      pkt.size = 0;
      pkt.write();
      IntByReference got_frame = new IntByReference();
      int ret = avcodec.avcodec_encode_video2(video_ctx, pkt, video_frame, got_frame);
      if (ret < 0) return false;
      if (got_frame.getValue() != 0 && pkt.size > 0) {
        pkt.stream_index = video_stream.index;
        pkt.write();  //writeField("stream_index");
        ret = avformat.av_interleaved_write_frame(fmt_ctx, pkt);
        avcodec.av_free_packet(pkt);
      }
      video_frame.pts++;
      video_frame.writeField("pts");
      return ret == 0;
    }
    /** Returns # samples per channel in frame. */
    public int get_audio_framesize() {
      if (audio_ctx == null) return 0;
      return audio_ctx.frame_size;
    }

    private boolean flush() {
      if (audio_frame == null) return false;
      AVPacket pkt = new AVPacket();
      avcodec.av_init_packet(pkt);
      pkt.read();
      pkt.data = null;
      pkt.size = 0;
      pkt.write();

      IntByReference got_frame = new IntByReference();
      int ret = avcodec.avcodec_encode_audio2(audio_ctx, pkt, null, got_frame);
      if (ret < 0) {
        System.out.println("avcodec_encode_audio2() failed");
        return false;
      }
      if (got_frame.getValue() != 0 && pkt.size > 0) {
        pkt.stream_index = audio_stream.index;
        pkt.writeField("stream_index");
        ret = avformat.av_interleaved_write_frame(fmt_ctx, pkt);
        avcodec.av_free_packet(pkt);
        if (ret < 0) System.out.println("av_interleaved_write_frame() failed");
        return ret == 0;
      }
      return false;
    }

    /** Stops encoder.  Freeing resources. */
    public void stop() {
      JFLog.log("Encoder.stop");
      //flush buffers
      if (samsbuf != null) {
        add_audio(samsbuf, 0, samsbuf.length);
        samsbuf = null;
      }
      //flush audio encoder
      while (flush()) {}
      avformat.av_write_trailer(fmt_ctx);
      if (audio_frame != null) {
        PointerByReference ref = new PointerByReference();
        ref.setValue(audio_frame.getPointer());
        avcodec.avcodec_free_frame(ref);
        audio_frame = null;
      }
      if (video_frame != null) {
        PointerByReference ref = new PointerByReference();
        ref.setValue(video_frame.getPointer());
        avcodec.avcodec_free_frame(ref);
        video_frame = null;
      }
      if (io_ctx != null) {
        avformat.avio_flush(io_ctx);
        avformat.avio_close(io_ctx);
        io_ctx = null;
        ff_buffer = null;
      }
      if (audio_stream != null) {
        avcodec.avcodec_close(audio_ctx);
        audio_stream = null;
      }
      if (video_stream != null) {
        avcodec.avcodec_close(video_ctx);
        video_stream = null;
      }
      if (fmt_ctx != null) {
        avformat.avformat_free_context(fmt_ctx);
        fmt_ctx = null;
      }
      if (src_pic != null) {
        avcodec.avpicture_free(src_pic);
        src_pic = null;
      }
      if (dst_pic != null) {
        avcodec.avpicture_free(dst_pic);
        dst_pic = null;
      }
      if (fmt_ctx != null) {
        avformat.avformat_free_context(fmt_ctx);
        fmt_ctx = null;
      }
      if (sws_ctx != null) {
        swscale.sws_freeContext(sws_ctx);
        sws_ctx = null;
      }
      if (swr_ctx != null) {
        if (libav_org)
          avresample.avresample_free(new PointerByReference(swr_ctx));
        else
          swresample.swr_free(new PointerByReference(swr_ctx));
        swr_ctx = null;
      }
    }
  }

  /** This class is NOT working yet. */
  public static class V4L2Camera implements javaforce.media.Camera.Input {
    private Decoder decoder;

    public boolean init() {
      return FFMPEG.init();
    }

    public boolean uninit() {
      return true;
    }

    public String[] listDevices() {
      //use LnxCamera to list devices
      LnxCamera lc = new LnxCamera();
      if (!lc.init()) return null;
      return lc.listDevices();
    }

    public boolean start(int deviceIdx, int width, int height) {
      JFLog.log("V4L2Camera:Using libavdevice");
      avutil.av_log_set_level(48);  //debug
      decoder = new Decoder();
      if (!decoder.start("/dev/video" + deviceIdx, "v4l2", width, height, -1, -1)) return false;
      return true;
    }

    public boolean stop() {
      if (decoder != null) {
        decoder.stop();
        decoder = null;
      }
      return true;
    }

    public int[] getFrame() {
      if (decoder == null) return null;
      decoder.read();
      return decoder.get_video();
    }

    public int getWidth() {
      if (decoder == null) return 0;
      return decoder.getWidth();
    }

    public int getHeight() {
      if (decoder == null) return 0;
      return decoder.getHeight();
    }
  }

  public static boolean download() {
    if (!Platform.isWindows()) {
      JF.showError("Notice", "This application requires the codecpack which was not detected.\n"
        + "Please visit http://javaforce.sourceforge.net/codecpack.php for more info.\n"
        + "Press OK to visit this page now");
      JF.openURL("http://javaforce.sourceforge.net/codecpack.php");
      return false;
    }
    if (!JF.showConfirm("Notice", "This application requires the codecpack which was not detected.\n"
      + "Please visit http://javaforce.sourceforge.net/codecpack.php for more info.\n"
      + "NOTE:To install the codecpack this app may require administrative rights.\n"
      + "To run with admin rights, right click this app and select 'Run as Administrator'.\n"
      + "Press OK to download and install now.\n"
      + "Press CANCEL to visit website now.\n"))
    {
      JF.openURL("http://javaforce.sourceforge.net/codecpack.php");
      return false;
    }
    JFTask task = new JFTask() {
      public boolean work() {
        this.setTitle("Downloading CodecPack");
        this.setLabel("Downloading CodecPack...");
        this.setProgress(-1);
        String destFolder = System.getenv("windir") + "\\system32";
        //find best place to extract to
        try {
          File file = new File(destFolder + "\\$testfile$.tmp");
          FileOutputStream fos = new FileOutputStream(file);
          fos.close();
          file.delete();
        } catch (Exception e) {
          JFLog.log("Warning:No access to Windows\\System32, extracting to current folder");
          destFolder = ".";
        }
        //first download latest URL from javaforce.sf.net
        try {
          BufferedReader reader = new BufferedReader(new InputStreamReader(
            new URL("http://javaforce.sourceforge.net/codecpack"
            + (Platform.is64Bit() ? "64" : "32") + ".php").openStream()));
          String url = reader.readLine();
          int fullLength = JF.atoi(reader.readLine());
          InputStream is = new URL(url).openStream();
          byte buf[] = new byte[64 * 1024];
          int length = 0;
          File file = new File(destFolder + "\\codecpack.zip");
          FileOutputStream fos = new FileOutputStream(file);
          while (true) {
            int read = is.read(buf);
            if (read == -1) break;
            if (read == 0) {
              JF.sleep(10);
              continue;
            }
            fos.write(buf, 0, read);
            length += read;
            this.setProgress(length * 100 / fullLength);
          }
          fos.close();
          if (length != fullLength) {
            this.setLabel("Download failed...");
            return false;
          }
          this.setLabel("Download complete, now installing...");
          this.setProgress(0);
          ZipFile zf = new ZipFile(file);
          int cnt = 0;
          for (Enumeration e = zf.entries(); e.hasMoreElements();) {
            ZipEntry ze = (ZipEntry)e.nextElement();
            String name = ze.getName().toLowerCase();
            if (!name.endsWith(".dll")) continue;
            int idx = name.lastIndexOf("/");
            if (idx != -1) {
              name = name.substring(idx+1);  //remove any path
            }
            fos = new FileOutputStream(destFolder + "\\" + name);
            InputStream zis = zf.getInputStream(ze);
            JFLog.log("extracting:" + name + ",length=" + ze.getSize());
            JF.copyAll(zis, fos, ze.getSize());
            zis.close();
            fos.close();
            cnt++;
            this.setProgress(cnt * 100 / 8);
          }
          this.setProgress(100);
          this.setLabel("Install complete");
          return true;
        } catch (Exception e) {
          this.setLabel("An error occured, see console output for details.");
          JFLog.log(e);
          return false;
        }
      }
    };
    new ProgressDialog(null, true, task).setVisible(true);
    return task.getStatus();
  }
}
