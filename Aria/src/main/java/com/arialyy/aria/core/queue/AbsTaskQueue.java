/*
 * Copyright (C) 2016 AriaLyy(https://github.com/AriaLyy/Aria)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arialyy.aria.core.queue;

import com.arialyy.aria.core.inf.IEntity;
import com.arialyy.aria.core.inf.ITask;
import com.arialyy.aria.core.inf.ITaskEntity;
import com.arialyy.aria.core.queue.pool.CachePool;
import com.arialyy.aria.core.queue.pool.ExecutePool;

/**
 * Created by Aria.Lao on 2017/2/23.
 */
abstract class AbsTaskQueue<TASK extends ITask, TASK_ENTITY extends ITaskEntity, ENTITY extends IEntity>
    implements ITaskQueue<TASK, TASK_ENTITY, ENTITY> {
  CachePool<TASK> mCachePool = new CachePool<>();
  ExecutePool<TASK> mExecutePool = new ExecutePool<>();
}
