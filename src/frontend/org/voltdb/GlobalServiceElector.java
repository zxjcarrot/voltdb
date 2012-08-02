/* This file is part of VoltDB.
 * Copyright (C) 2008-2012 VoltDB Inc.
 *
 * VoltDB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VoltDB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with VoltDB.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.voltdb;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.zookeeper_voltpatches.KeeperException;
import org.apache.zookeeper_voltpatches.ZooKeeper;

import org.voltcore.zk.LeaderElector;
import org.voltcore.zk.LeaderNoticeHandler;

/**
 * GlobalServiceElector performs leader election to determine which VoltDB cluster node
 * will be responsible for leading various cluster-wide services, particularly those which must
 * run on the same node.
 */
class GlobalServiceElector implements LeaderNoticeHandler
{
    private final LeaderElector m_leaderElector;
    private final List<Promotable> m_services = new ArrayList<Promotable>();

    GlobalServiceElector(ZooKeeper zk)
    {
        m_leaderElector = new LeaderElector(zk, VoltZK.leaders_globalservice,
                "globalservice", null, this);
    }

    /** Add a service to be notified if this node becomes the global leader */
    synchronized void registerService(Promotable service)
    {
        m_services.add(service);
    }

    /** Kick off the leader election */
    void start() throws KeeperException, InterruptedException, ExecutionException
    {
        m_leaderElector.start(true);
    }

    @Override
    synchronized public void becomeLeader()
    {
        for (Promotable service : m_services) {
            service.acceptPromotion();
        }
    }

    void shutdown() throws InterruptedException, KeeperException
    {
        m_leaderElector.shutdown();
    }
}
