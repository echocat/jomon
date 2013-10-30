/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012-2013 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.process.sigar;

import org.echocat.jomon.process.AccessDeniedException;
import org.hyperic.sigar.*;

import java.io.File;
import java.util.List;
import java.util.Map;

public class SigarFacade implements AutoCloseable {

    private final Sigar _sigar = new Sigar();

    public long getPid() {
        return _sigar.getPid();
    }

    public long getServicePid(String s) throws SigarException {return _sigar.getServicePid(s);}

    public void kill(long l, int i) {
        try {
            _sigar.kill(l, i);
        } catch (SigarPermissionDeniedException e) {
            throw new AccessDeniedException("Could not kill: " + l, e);
        } catch (SigarException e) {
            throw new IllegalArgumentException("Could not kill: " + l, e);
        }
    }

    public void kill(long pid, String signame) throws SigarException {_sigar.kill(pid, signame);}

    public void kill(String pid, int signum) throws SigarException {_sigar.kill(pid, signum);}

    public Mem getMem() throws SigarException {return _sigar.getMem();}

    public Swap getSwap() throws SigarException {return _sigar.getSwap();}

    public Cpu getCpu() throws SigarException {return _sigar.getCpu();}

    public CpuPerc getCpuPerc() throws SigarException {return _sigar.getCpuPerc();}

    public CpuPerc[] getCpuPercList() throws SigarException {return _sigar.getCpuPercList();}

    public ResourceLimit getResourceLimit() throws SigarException {return _sigar.getResourceLimit();}

    public Uptime getUptime() throws SigarException {return _sigar.getUptime();}

    public double[] getLoadAverage() throws SigarException {return _sigar.getLoadAverage();}

    public long[] getProcList() throws SigarException {return _sigar.getProcList();}

    public ProcStat getProcStat() throws SigarException {return _sigar.getProcStat();}

    public ProcMem getProcMem(long pid) throws SigarException {return _sigar.getProcMem(pid);}

    public ProcMem getProcMem(String pid) throws SigarException {return _sigar.getProcMem(pid);}

    public ProcMem getMultiProcMem(String query) throws SigarException {return _sigar.getMultiProcMem(query);}

    public ProcState getProcState(long pid) throws SigarException {return _sigar.getProcState(pid);}

    public ProcState getProcState(String pid) throws SigarException {return _sigar.getProcState(pid);}

    public ProcTime getProcTime(long pid) throws SigarException {return _sigar.getProcTime(pid);}

    public ProcTime getProcTime(String pid) throws SigarException {return _sigar.getProcTime(pid);}

    public ProcCpu getProcCpu(long pid) throws SigarException {return _sigar.getProcCpu(pid);}

    public ProcCpu getProcCpu(String pid) throws SigarException {return _sigar.getProcCpu(pid);}

    public MultiProcCpu getMultiProcCpu(String query) throws SigarException {return _sigar.getMultiProcCpu(query);}

    public ProcCred getProcCred(long pid) throws SigarException {return _sigar.getProcCred(pid);}

    public ProcCred getProcCred(String pid) throws SigarException {return _sigar.getProcCred(pid);}

    public ProcCredName getProcCredName(long pid) throws SigarException {return _sigar.getProcCredName(pid);}

    public ProcCredName getProcCredName(String pid) throws SigarException {return _sigar.getProcCredName(pid);}

    public ProcFd getProcFd(long pid) throws SigarException {return _sigar.getProcFd(pid);}

    public ProcFd getProcFd(String pid) throws SigarException {return _sigar.getProcFd(pid);}

    public ProcExe getProcExe(long pid) throws SigarException {return _sigar.getProcExe(pid);}

    public ProcExe getProcExe(String pid) throws SigarException {return _sigar.getProcExe(pid);}

    public String[] getProcArgs(long l) throws SigarException {return _sigar.getProcArgs(l);}

    public String[] getProcArgs(String pid) throws SigarException {return _sigar.getProcArgs(pid);}

    public Map<?, ?> getProcEnv(long pid) throws SigarException {return _sigar.getProcEnv(pid);}

    public Map<?, ?> getProcEnv(String pid) throws SigarException {return _sigar.getProcEnv(pid);}

    public String getProcEnv(long pid, String key) throws SigarException {return _sigar.getProcEnv(pid, key);}

    public String getProcEnv(String pid, String key) throws SigarException {return _sigar.getProcEnv(pid, key);}

    public List<?> getProcModules(long pid) throws SigarException {return _sigar.getProcModules(pid);}

    public List<?> getProcModules(String pid) throws SigarException {return _sigar.getProcModules(pid);}

    public long getProcPort(int i, long l) throws SigarException {return _sigar.getProcPort(i, l);}

    public long getProcPort(String protocol, String port) throws SigarException {return _sigar.getProcPort(protocol, port);}

    public ThreadCpu getThreadCpu() throws SigarException {return _sigar.getThreadCpu();}

    public FileSystem[] getFileSystemList() throws SigarException {return _sigar.getFileSystemList();}

    public FileSystemUsage getFileSystemUsage(String name) throws SigarException {return _sigar.getFileSystemUsage(name);}

    public DiskUsage getDiskUsage(String name) throws SigarException {return _sigar.getDiskUsage(name);}

    public FileSystemUsage getMountedFileSystemUsage(String name) throws SigarException {return _sigar.getMountedFileSystemUsage(name);}

    public FileSystemMap getFileSystemMap() throws SigarException {return _sigar.getFileSystemMap();}

    public FileInfo getFileInfo(String name) throws SigarException {return _sigar.getFileInfo(name);}

    public FileInfo getLinkInfo(String name) throws SigarException {return _sigar.getLinkInfo(name);}

    public DirStat getDirStat(String name) throws SigarException {return _sigar.getDirStat(name);}

    public DirUsage getDirUsage(String name) throws SigarException {return _sigar.getDirUsage(name);}

    public CpuInfo[] getCpuInfoList() throws SigarException {return _sigar.getCpuInfoList();}

    public Cpu[] getCpuList() throws SigarException {return _sigar.getCpuList();}

    public NetRoute[] getNetRouteList() throws SigarException {return _sigar.getNetRouteList();}

    public NetConnection[] getNetConnectionList(int i) throws SigarException {return _sigar.getNetConnectionList(i);}

    public String getNetListenAddress(long l) throws SigarException {return _sigar.getNetListenAddress(l);}

    public String getNetListenAddress(String port) throws SigarException {return _sigar.getNetListenAddress(port);}

    public String getNetServicesName(int i, long l) {return _sigar.getNetServicesName(i, l);}

    public NetStat getNetStat() throws SigarException {return _sigar.getNetStat();}

    public NetStat getNetStat(byte[] address, long port) throws SigarException {return _sigar.getNetStat(address, port);}

    public Who[] getWhoList() throws SigarException {return _sigar.getWhoList();}

    public Tcp getTcp() throws SigarException {return _sigar.getTcp();}

    public NfsClientV2 getNfsClientV2() throws SigarException {return _sigar.getNfsClientV2();}

    public NfsServerV2 getNfsServerV2() throws SigarException {return _sigar.getNfsServerV2();}

    public NfsClientV3 getNfsClientV3() throws SigarException {return _sigar.getNfsClientV3();}

    public NfsServerV3 getNfsServerV3() throws SigarException {return _sigar.getNfsServerV3();}

    public NetInfo getNetInfo() throws SigarException {return _sigar.getNetInfo();}

    public NetInterfaceConfig getNetInterfaceConfig(String name) throws SigarException {return _sigar.getNetInterfaceConfig(name);}

    public NetInterfaceConfig getNetInterfaceConfig() throws SigarException {return _sigar.getNetInterfaceConfig();}

    public NetInterfaceStat getNetInterfaceStat(String name) throws SigarException {return _sigar.getNetInterfaceStat(name);}

    public String[] getNetInterfaceList() throws SigarException {return _sigar.getNetInterfaceList();}

    public String getFQDN() throws SigarException {return _sigar.getFQDN();}

    public void enableLogging(boolean value) {_sigar.enableLogging(value);}

    public File getNativeLibrary() {return _sigar.getNativeLibrary();}

    @Override
    public void close() {_sigar.close();}
}
