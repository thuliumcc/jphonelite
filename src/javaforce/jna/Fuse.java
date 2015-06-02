package javaforce.jna;

/** Fuse base class
 * To create a Fuse system, extend this class and override the fuse system calls.
 * Call init() to load the fuse library then call start(String args[]) to start.
 * Then use 'fusermount -u mount_point' to terminate the server.
 * Make sure to use '-f' option or this crashes! (Java doesn't like detach from terminal)
 *
 * General usage: jfuse... resource mount_point [options]
 *
 * @author pquiring
 *
 * Created : Nov 1, 2013
 */

import java.util.*;
import java.lang.reflect.*;

import com.sun.jna.*;

import javaforce.*;

public class Fuse {
  public static class Stat {
    public boolean folder;
    public boolean symlink;
    public int mode;  //rwx rwx rwx
    public long size;
    public long atime, mtime, ctime;  //unix time
  }
  //NOTE:Fuse requires "#define _FILE_OFFSET_BITS 64" which causes strange changes to some structures
  public static class Stat32 extends Structure {
    public long st_dev;  //ignored       //0 offset
    public long moved;  //field moved (see bits/stat.h)
    public int st_mode;                  //16
    public int st_nlink;                 //20
    public int st_uid;                   //24
    public int st_gid;                   //28
    public long st_rdev;  //???          //32
      public int pad1;  //strange padding
    public int st_size;                  //44
      public int pad2;  //strange padding
    public int st_blksize;  //ignored    //52
    public long st_blocks;               //56
    public long st_atime;                //64
    public long st_mtime;                //72
    public long st_ctime;                //80
    public long st_ino;  //ignored ???   //88  //moved

    protected List getFieldOrder() {
      return makeFieldList(getClass());
    }
    public int fieldOffset(String name) {
      return super.fieldOffset(name);
    }
    public Stat32() {}
    public Stat32(Pointer ptr) {
      super(ptr);
    }
  }

  //Stat64 untested!!!
  public static class Stat64 extends Structure {
    public long st_dev;  //ignored
    public long st_ino;  //ignored ???
    public long st_nlink;
    public int st_mode;
    public int st_uid;
    public int st_gid;
      public int pad1;
    public long st_rdev;
    public long st_size;
    public long st_blksize;  //ignored
    public long st_blocks;
    public long st_atime;
      public long st_atimensec;
    public long st_mtime;
      public long st_mtimensec;
    public long st_ctime;
      public long st_ctimensec;

    protected List getFieldOrder() {
      return makeFieldList(getClass());
    }
    public int fieldOffset(String name) {
      return super.fieldOffset(name);
    }
    public Stat64() {}
    public Stat64(Pointer ptr) {
      super(ptr);
    }
  }

  private static final int S_IFDIR = 0040000;
  private static final int S_IFLNK = 0020000;  //NOTE:doesn't include S_IFREG
  private static final int S_IFREG = 0100000;

  public static class Statvfs32 extends Structure {
    public int f_bsize;
    public int f_frsize;  //ignored
    public long f_blocks;
    public long f_bfree;
    public long f_bavail;
    public long f_files;
    public long f_ffree;
    public long f_favail;  //ignored
    public int f_fsid;  //ignored
    public int f_flag;  //ignored
    public int f_namemax;

    protected List getFieldOrder() {
      return makeFieldList(getClass());
    }
    public int fieldOffset(String name) {
      return super.fieldOffset(name);
    }
    public Statvfs32() {}
    public Statvfs32(Pointer ptr) {
      super(ptr);
    }
  }
  private static Statvfs32 statvfs32 = new Statvfs32();

  public static class Statvfs64 extends Structure {
    public long f_bsize;
    public long f_frsize;  //ignored
    public long f_blocks;
    public long f_bfree;
    public long f_bavail;
    public long f_files;
    public long f_ffree;
    public long f_favail;  //ignored
    public long f_fsid;  //ignored
    public long f_flag;  //ignored
    public long f_namemax;

    protected List getFieldOrder() {
      return makeFieldList(getClass());
    }
    public int fieldOffset(String name) {
      return super.fieldOffset(name);
    }
    public Statvfs64() {}
    public Statvfs64(Pointer ptr) {
      super(ptr);
    }
  }
  private static Statvfs64 statvfs64 = new Statvfs64();

  public static class FuseFileInfo32 extends Structure {
    public int flags;
    public int fh_old;
    public int writepage;
    public int flags2;  //direct_io, keep_cache, flush, nonseekable, flock_release, padding
    public long fh;
    public long lock_owner;
    public int poll_events;

    protected List getFieldOrder() {
      return makeFieldList(getClass());
    }
    public int fieldOffset(String name) {
      return super.fieldOffset(name);
    }
    public FuseFileInfo32() {}
    public FuseFileInfo32(Pointer ptr) {
      super(ptr);
    }
  }

  public static class FuseFileInfo64 extends Structure {
    public int flags;
    public long fh_old;
    public int writepage;
    public int flags2;  //direct_io, keep_cache, flush, nonseekable, flock_release, padding
    public long fh;
    public long lock_owner;
//    public int poll_events;  //3.0

    protected List getFieldOrder() {
      return makeFieldList(getClass());
    }
    public int fieldOffset(String name) {
      return super.fieldOffset(name);
    }
    public FuseFileInfo64() {}
    public FuseFileInfo64(Pointer ptr) {
      super(ptr);
    }
  }

  public int invokeFiller(Pointer filler, Pointer buf, String name, Pointer stat) {
    if (name.endsWith("/")) name = name.substring(0, name.length() - 1);
    Function func = Function.getFunction(filler);
    return func.invokeInt(new Object[] {buf, name, stat, new Long(0)});
  }

  public HashMap<Long, Object> handles = new HashMap<Long, Object>();

  private long nextId = 100;
  private synchronized long getNextId() {
    return nextId++;
  }

  public void attachObject(Pointer ffi, Object obj) {
    long id = getNextId();
    handles.put(id, obj);
    if (Pointer.SIZE == 4) {
      FuseFileInfo32 ffi32 = new FuseFileInfo32(ffi);
      ffi32.fh = id;
      ffi32.writeField("fh");
    } else {
      FuseFileInfo32 ffi64 = new FuseFileInfo32(ffi);
      ffi64.fh = id;
      ffi64.writeField("fh");
    }
    JFLog.log("attachObject=" + id + ":ffi=" + ffi);
  }

  public Object getObject(Pointer ffi) {
    long id;
    if (Pointer.SIZE == 4) {
      FuseFileInfo32 ffi32 = new FuseFileInfo32(ffi);
      ffi32.read();
      id = ffi32.fh;
    } else {
      FuseFileInfo32 ffi64 = new FuseFileInfo32(ffi);
      ffi64.read();
      id = ffi64.fh;
    }
    JFLog.log("getObject=" + id + ":ffi=" + ffi);
    return handles.get(id);
  }

  public void detachObject(Pointer ffi) {
    long id;
    if (Pointer.SIZE == 4) {
      FuseFileInfo32 ffi32 = new FuseFileInfo32(ffi);
      ffi32.read();
      id = ffi32.fh;
    } else {
      FuseFileInfo32 ffi64 = new FuseFileInfo32(ffi);
      ffi64.read();
      id = ffi64.fh;
    }
    JFLog.log("detachObject=" + id);
    handles.remove(id);
  }

  public interface destroy extends Callback {
    int callback(Pointer ptr);
  }
  public interface init extends Callback {
    Pointer callback(Pointer ptr);
  }

  public int getattr(String path, Stat stat) {return -1;}
  public interface getattr extends Callback {
    int callback(Pointer path, Pointer stat);
  }

  public int mkdir(String path, int mode) {return -1;}
  public interface mkdir extends Callback {
    int callback(Pointer path, int mode);
  }

  public int unlink(String path) {return -1;}
  public interface unlink extends Callback {
    int callback(Pointer path);
  }

  public int rmdir(String path) {return -1;}
  public interface rmdir extends Callback {
    int callback(Pointer path);
  }

  public int symlink(String target, String link) {return -1;}
  public interface symlink extends Callback {
    int callback(Pointer target, Pointer link);
  }

  public int link(String target, String link) {return -1;}
  public interface link extends Callback {
    int callback(Pointer target, Pointer link);
  }

  public int chmod(String path, int mode) {return -1;}
  public interface chmod extends Callback {
    int callback(Pointer path, int mode);
  }

  public int chown(String path, int uid, int gid) {return -1;}
  public interface chown extends Callback {
    int callback(Pointer path, int mode, int gid);
  }

  public int truncate(String path, long size) {return -1;}
  public interface truncate32 extends Callback {
    int callback(Pointer path, int mode);
  }
  public interface truncate64 extends Callback {
    int callback(Pointer path, long mode);
  }

  public int open(String path, Pointer ffi) {return -1;}
  public interface open extends Callback {
    int callback(Pointer path, Pointer ffi);
  }

  public int read(String path, Pointer buf, int size, long offset, Pointer ffi) {return -1;}
  public interface read32 extends Callback {
    int callback(Pointer path, Pointer buf, int size, long offset, Pointer ffi);
  }
  public interface read64 extends Callback {
    int callback(Pointer path, Pointer buf, long size, long offset, Pointer ffi);
  }

  public int write(String path, Pointer buf, int size, long offset, Pointer ffi) {return -1;}
  public interface write32 extends Callback {
    int callback(Pointer path, Pointer buf, int size, long offset, Pointer ffi);
  }
  public interface write64 extends Callback {
    int callback(Pointer path, Pointer buf, long size, long offset, Pointer ffi);
  }

  public int statfs(String path, Pointer statvfs) {return -1;}
  public interface statfs extends Callback {
    int callback(Pointer path, Pointer statvfs);
  }

  public int release(String path, Pointer ffi) {return -1;}
  public interface release extends Callback {
    int callback(Pointer path, Pointer ffi);
  }

  public interface setxattr32 extends Callback {
    int callback(Pointer path, Pointer name, Pointer value, int size, int flags);
  }
  public interface setxattr64 extends Callback {
    int callback(Pointer path, Pointer name, Pointer value, long size, int flags);
  }

  public interface getxattr32 extends Callback {
    int callback(Pointer path, Pointer name, Pointer value, int size);
  }
  public interface getxattr64 extends Callback {
    int callback(Pointer path, Pointer name, Pointer value, long size);
  }

  public interface listxattr32 extends Callback {
    int callback(Pointer path, Pointer name, int size);
  }
  public interface listxattr64 extends Callback {
    int callback(Pointer path, Pointer name, long size);
  }

  public int readdir(String path, Pointer buf, Pointer filler, Pointer ffi) {return -1;}
  public interface readdir extends Callback {
    int callback(Pointer path, Pointer buf, Pointer filler, long offset, Pointer ffi);
  }

  public int create(String path, int mode, Pointer ffi) {return -1;}
  public interface create extends Callback {
    int callback(Pointer path, int mode, Pointer ffi);
  }

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

  public class fuse_operations extends Structure {
    public Callback getattr;
    public Callback readlink;
    public Callback getdir;  //deprecated -> readdir
    public Callback mknod;  //or use create
    public Callback mkdir;
    public Callback unlink;
    public Callback rmdir;
    public Callback symlink;
    public Callback rename;
    public Callback link;
    public Callback chmod;
    public Callback chown;
    public Callback truncate;
    public Callback utime;  //deprecated -> utimens
    public Callback open;
    public Callback read;
    public Callback write;
    public Callback statfs;
    public Callback flush;
    public Callback release;
    public Callback fsync;
    public Callback setxattr;
    public Callback getxattr;
    public Callback listxattr;
    public Callback removexattr;
    public Callback opendir;
    public Callback readdir;
    public Callback releasedir;
    public Callback fsyncdir;
    public Callback init;
    public Callback destroy;
    public Callback access;
    public Callback create;  //or mknod
    public Callback ftruncate;
    public Callback fgetattr;
    public Callback lock;
    public Callback utimens;
    public Callback bmap;
    public int flags;
    public Callback ioctl;
    public Callback poll;
    public Callback write_buf;
    public Callback read_buf;
    public Callback flock;
    public Callback fallocate;

    protected List getFieldOrder() {
      return makeFieldList(getClass());
    }
    public int fieldOffset(String name) {
      return super.fieldOffset(name);
    }
  }

  public class fuse_init extends Structure {
    public int id;  //not used for now

    protected List getFieldOrder() {
      return makeFieldList(getClass());
    }
  }

  public interface FuseLibrary extends Library {
    public int fuse_main_real(int argc, String argv[], fuse_operations ops, int size_ops, Pointer user_data);
  }

  private static FuseLibrary fuse;

  /** Must call once per process. */
  public static boolean init() {
    if (fuse != null) return true;
    try {
      fuse = (FuseLibrary)Native.loadLibrary("fuse", FuseLibrary.class);
    } catch (Throwable t) {
      JFLog.log(t);
      return false;
    }
    return true;
  }

  private static final long y2k = 946759978;  //Jan 1st, 2000

  //must return -errno
  public static final int EPERM = 1;  //not permitted
  public static final int ENOENT = 2;  //file/dir not found
  public static final int EIO = 5;  //input/output error
  public static final int EACCESS = 13;  //access denied
  public static final int EEXIST = 17;  //file already exists
  public static final int ENOTDIR = 20;  //not a directory
  public static final int EISDIR = 21;  //is a directory
  public static final int EINVAL = 22;  //invalid
  public static final int EFBIG = 27;  //file too large
  public static final int ENOSPC = 28;  //device out of space
  public static final int EROFS = 30;  //read only file system
  //etc.
  public static final int ENOTSUP = 95;  //not supported

  public boolean start(String args[]) {
    fuse_operations ops = new fuse_operations();
    //force -f option (without it this crashes)
    boolean f = false;
    for(int a=0;a<args.length;a++) {
      if (args[a].equals("-f")) {f = true; break;}
    }
    if (!f) {
      args = Arrays.copyOf(args, args.length+1);
      args[args.length-1] = "-f";
    }
/*
    ops.init = new init() {
      public Pointer callback(Pointer ptr) {
        JFLog.log("Init...");
        return null;
      }
    };
    ops.destroy = new destroy() {
      public int callback(Pointer ptr) {
        JFLog.log("Exiting...");
        System.exit(0);
        return 0;
      }
    };
*/
    ops.getattr = new getattr() {
      public int callback(Pointer path, Pointer stat) {
        Stat _stat = new Stat();
        int ret = getattr(path.getString(0), _stat);
        //set some defaults
        if (_stat.atime == 0) _stat.atime = y2k;
        if (_stat.mtime == 0) _stat.mtime = y2k;
        if (_stat.ctime == 0) _stat.ctime = y2k;
        if (_stat.mode == 0) _stat.mode = 0777;  //appear wide open
        if (_stat.folder) {
          _stat.mode += S_IFDIR;
        } else {
          _stat.mode += S_IFREG;
        }
        if (_stat.symlink) _stat.mode += S_IFLNK;
        long blocks = _stat.size / 512;
        if (_stat.size % 512 != 0) blocks++;
        if (Pointer.SIZE == 4) {
          Stat32 stat32 = new Stat32(stat);
          stat32.st_mode = _stat.mode;
          stat32.st_nlink = 1;
          stat32.st_uid = 0;  //root
          stat32.st_gid = 0;  //root
          stat32.st_size = (int)_stat.size;
          stat32.st_blocks = (int)blocks;
          stat32.st_atime = _stat.atime;
          stat32.st_mtime = _stat.mtime;
          stat32.st_ctime = _stat.ctime;
          stat32.write();
        } else {
          Stat64 stat64 = new Stat64(stat);
          stat64.st_mode = _stat.mode;
          stat64.st_nlink = 1;
          stat64.st_uid = 0;  //root
          stat64.st_gid = 0;  //root
          stat64.st_size = _stat.size;
          stat64.st_blocks = blocks;
          stat64.st_atime = _stat.atime;
          stat64.st_mtime = _stat.mtime;
          stat64.st_ctime = _stat.ctime;
          stat64.write();
        }
        return ret;
      }
    };
    ops.mkdir = new mkdir() {
      public int callback(Pointer path, int mode) {
        return mkdir(path.getString(0), mode);
      }
    };
    ops.unlink = new unlink() {
      public int callback(Pointer path) {
        return unlink(path.getString(0));
      }
    };
    ops.rmdir = new rmdir() {
      public int callback(Pointer path) {
        return rmdir(path.getString(0));
      }
    };
    ops.symlink = new symlink() {
      public int callback(Pointer target, Pointer link) {
        return symlink(target.getString(0), link.getString(0));
      }
    };
    ops.link = new link() {
      public int callback(Pointer target, Pointer link) {
        return link(target.getString(0), link.getString(0));
      }
    };
    ops.chmod = new chmod() {
      public int callback(Pointer path, int mode) {
        return chmod(path.getString(0), mode);
      }
    };
    ops.chown = new chown() {
      public int callback(Pointer path, int uid, int gid) {
        return chown(path.getString(0), uid, gid);
      }
    };
    if (Pointer.SIZE == 4) {
      ops.truncate = new truncate32() {
        public int callback(Pointer path, int size) {
          return truncate(path.getString(0), size);
        }
      };
    } else {
      ops.truncate = new truncate64() {
        public int callback(Pointer path, long size) {
          return truncate(path.getString(0), size);
        }
      };
    }
    ops.open = new open() {
      public int callback(Pointer path, Pointer ffi) {
        return open(path.getString(0), ffi);
      }
    };
    if (Pointer.SIZE == 4) {
      ops.read = new read32() {
        public int callback(Pointer path, Pointer buf, int size, long offset, Pointer ffi) {
          return read(path.getString(0), buf, size, offset, ffi);
        }
      };
    } else {
      ops.read = new read64() {
        public int callback(Pointer path, Pointer buf, long size, long offset, Pointer ffi) {
          return read(path.getString(0), buf, (int)size, offset, ffi);
        }
      };
    }
    if (Pointer.SIZE == 4) {
      ops.write = new write32() {
        public int callback(Pointer path, Pointer buf, int size, long offset, Pointer ffi) {
          return write(path.getString(0), buf, size, offset, ffi);
        }
      };
    } else {
      ops.write = new write64() {
        public int callback(Pointer path, Pointer buf, long size, long offset, Pointer ffi) {
          return write(path.getString(0), buf, (int)size, offset, ffi);
        }
      };
    }
    ops.statfs = new statfs() {
      public int callback(Pointer path, Pointer statvfs) {
        return statfs(path.getString(0), statvfs);
      }
    };
    ops.release = new release() {
      public int callback(Pointer path, Pointer ffi) {
        return release(path.getString(0), ffi);
      }
    };

    if (Pointer.SIZE == 4) {
      ops.setxattr = new setxattr32() {
        public int callback(Pointer path, Pointer name, Pointer value, int size, int flags) {
          JFLog.log("setxattr32:" + path.getString(0) + ":" + name.getString(0));
          return -ENOTSUP;
        }
      };
    } else {
      ops.setxattr = new setxattr64() {
        public int callback(Pointer path, Pointer name, Pointer value, long size, int flags) {
          JFLog.log("setxattr64:" + path.getString(0) + ":" + name.getString(0));
          return -ENOTSUP;
        }
      };
    }
    if (Pointer.SIZE == 4) {
      ops.getxattr = new getxattr32() {
        public int callback(Pointer path, Pointer name, Pointer value, int size) {
          JFLog.log("getxattr32:" + path.getString(0) + ":" + name.getString(0));
          return -ENOTSUP;
        }
      };
    } else {
      ops.getxattr = new getxattr64() {
        public int callback(Pointer path, Pointer name, Pointer value, long size) {
          JFLog.log("getxattr64:" + path.getString(0) + ":" + name.getString(0));
          return -ENOTSUP;
        }
      };
    }
    if (Pointer.SIZE == 4) {
      ops.listxattr = new listxattr32() {
        public int callback(Pointer path, Pointer name, int size) {
          JFLog.log("listxattr32");
          return -ENOTSUP;
        }
      };
    } else {
      ops.listxattr = new listxattr64() {
        public int callback(Pointer path, Pointer name, long size) {
          JFLog.log("listxattr64");
          return -ENOTSUP;
        }
      };
    }

    ops.readdir = new readdir() {
      public int callback(Pointer path, Pointer buf, Pointer filler, long offset, Pointer ffi) {
        return readdir(path.getString(0), buf, filler, ffi);
      }
    };
    ops.create = new create() {
      public int callback(Pointer path, int mode, Pointer ffi) {
        return create(path.getString(0), mode, ffi);
      }
    };

    fuse.fuse_main_real(args.length, args, ops, ops.size(), null); //new fuse_init().getPointer());
    return true;
  }
}
