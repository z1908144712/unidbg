package com.wuaipojie.crackme02;

import com.github.unidbg.Emulator;
import com.github.unidbg.Module;
import com.github.unidbg.Symbol;
import com.github.unidbg.arm.ARMEmulator;
import com.github.unidbg.arm.HookStatus;
import com.github.unidbg.arm.context.RegisterContext;
import com.github.unidbg.debugger.DebuggerType;
import com.github.unidbg.hook.ReplaceCallback;
import com.github.unidbg.hook.whale.IWhale;
import com.github.unidbg.hook.whale.Whale;
import com.github.unidbg.hook.xhook.IxHook;
import com.github.unidbg.linux.android.AndroidARMEmulator;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.XHookImpl;
import com.github.unidbg.linux.android.dvm.DalvikModule;
import com.github.unidbg.linux.android.dvm.DvmClass;
import com.github.unidbg.linux.android.dvm.StringObject;
import com.github.unidbg.linux.android.dvm.VM;
import com.github.unidbg.memory.Memory;
import com.github.unidbg.utils.Inspector;
import com.sun.jna.Pointer;

import java.io.File;
import java.io.IOException;

public class CheckSn {
    private final ARMEmulator emulator;
    private final VM vm;
    private final Module module;
    private  final Memory memory;

    private final DvmClass MainActivity;

    private CheckSn() throws IOException {
        emulator = new AndroidARMEmulator("com.wuaipojie.crackme02");
        memory = emulator.getMemory();
        memory.setLibraryResolver(new AndroidResolver(19));
        memory.setCallInitFunction();

        vm = emulator.createDalvikVM(null);
        vm.setVerbose(true);
        DalvikModule dm = vm.loadLibrary(new File("src/test/resources/example_binaries/armeabi-v7a/libxtian.so"), false);
        dm.callJNI_OnLoad(emulator);
        module = dm.getModule();

        MainActivity = vm.resolveClass("com/wuaipojie/crackme02/MainActivity");
    }

    private void destroy() throws IOException {
        emulator.close();
        System.out.println("destroy");
    }

    public static void main(String[] args) throws Exception {
        CheckSn test = new CheckSn();

        test.test();

        test.destroy();
    }

    private void test() {

        IxHook xHook = XHookImpl.getInstance(emulator);
        xHook.register("libc.so", "strlen", new ReplaceCallback() {
            @Override
            public HookStatus onCall(Emulator emulator, long originFunction) {
                Pointer pointer = emulator.getContext().getPointerArg(0);
                System.out.println("strlen=" + pointer.getString(0));
                return HookStatus.RET(emulator, originFunction);
            }
        });
        xHook.register("libc.so", "malloc", new ReplaceCallback() {
            @Override
            public HookStatus onCall(Emulator emulator, long originFunction) {
                int size = emulator.getContext().getIntArg(0);
                System.out.println("malloc=" + size);
                return HookStatus.RET(emulator, originFunction);
            }
        });
        xHook.register("libc.so", "memcpy", new ReplaceCallback() {
            @Override
            public HookStatus onCall(Emulator emulator, long originFunction) {
                RegisterContext context = emulator.getContext();
                Pointer dest = context.getPointerArg(0);
                Pointer src = context.getPointerArg(1);
                int length = context.getIntArg(2);
                Inspector.inspect(src.getByteArray(0, length), "memcpy dest=" + dest);
                return HookStatus.RET(emulator, originFunction);
            }
        });
        xHook.refresh();

        IWhale whale = Whale.getInstance(emulator);
        whale.WInlineHookFunction(emulator.getMemory().findModule("libc.so").findSymbolByName("strlen"), new ReplaceCallback() {
            @Override
            public HookStatus onCall(Emulator emulator, long originFunction) {
                Pointer pointer = emulator.getContext().getPointerArg(0);
                String s = pointer.getString(0);
                Inspector.inspect(s.getBytes(), "strlen( " +s+" )");
                return HookStatus.RET(emulator, originFunction);
            }
        });
        whale.WInlineHookFunction(emulator.getMemory().findModule("libc.so").findSymbolByName("malloc"), new ReplaceCallback() {
            @Override
            public HookStatus onCall(Emulator emulator, long originFunction) {
                int size = emulator.getContext().getIntArg(0);
                System.out.println("malloc=" + size);
                return HookStatus.RET(emulator, originFunction);
            }
        });
        whale.WInlineHookFunction(emulator.getMemory().findModule("libc.so").findSymbolByName("memcpy"), new ReplaceCallback() {
            @Override
            public HookStatus onCall(Emulator emulator, long originFunction) {
                RegisterContext context = emulator.getContext();
                Pointer dest = context.getPointerArg(0);
                Pointer src = context.getPointerArg(1);
                int length = context.getIntArg(2);
                Inspector.inspect(src.getByteArray(0, length), "memcpy dest=" + dest);
                return HookStatus.RET(emulator, originFunction);
            }
        });
        whale.WInlineHookFunction(emulator.getMemory().findModule("libc.so").findSymbolByName("realloc"), new ReplaceCallback() {
            @Override
            public HookStatus onCall(Emulator emulator, long originFunction) {
                RegisterContext context = emulator.getContext();
                Pointer src = context.getPointerArg(0);
                int length = context.getIntArg(1);
                Inspector.inspect(src.getByteArray(0, length), "realloc");
                return HookStatus.RET(emulator, originFunction);
            }
        });

        long start = System.currentTimeMillis();
        String input_uid = "2020206";
        String input_flag = "52pojie2020xtian=";
//        emulator.attach(DebuggerType.GDB_SERVER);
//        emulator.attach(DebuggerType.SIMPLE);
//        File file = new File("C:\\Users\\UnknownError\\Desktop\\codes.txt");
//        emulator.redirectTrace(file);
//        emulator.traceCode();
//        System.out.println("libc=" + memory.findModule("libc.so"));
//        emulator.attach().addBreakPoint(module, 0x0001509c);
        Number ret = MainActivity.callStaticJniMethod(emulator, "checkSn(Ljava/lang/String;Ljava/lang/String;)Z", vm.addLocalObject(new StringObject(vm, input_uid)), vm.addLocalObject(new StringObject(vm, input_flag)));
        vm.deleteLocalRefs();
        System.out.println("checkSn ret=" + ret + ", offset=" + (System.currentTimeMillis() - start) + "ms");
    }
}
