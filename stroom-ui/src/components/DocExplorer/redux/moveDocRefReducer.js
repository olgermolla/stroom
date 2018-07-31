/*
 * Copyright 2018 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { createActions, combineActions, handleActions } from 'redux-actions';

import { actionCreators as explorerTreeActionCreators } from './explorerTreeReducer';

const { docRefsMoved } = explorerTreeActionCreators;

const actionCreators = createActions({
  PREPARE_DOC_REF_MOVE: uuids => ({ uuids }),
  COMPLETE_DOC_REF_MOVE: () => ({ uuids: [] }),
});

const { prepareDocRefMove, completeDocRefMove } = actionCreators;

// Array of doc refs being moved
const defaultState = { isMoving: false, uuids: [] };

const reducer = handleActions(
  {
    [combineActions(prepareDocRefMove, completeDocRefMove)]: (state, { payload: { uuids } }) => ({
      isMoving: uuids.length > 0,
      uuids,
    }),
    [docRefsMoved]: (state, action) => ({
      isMoving: false,
      uuids: [],
    }),
  },
  defaultState,
);

export { actionCreators, reducer };
