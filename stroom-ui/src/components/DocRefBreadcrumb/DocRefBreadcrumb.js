import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { compose, branch, renderNothing } from 'recompose';
import { Breadcrumb } from 'semantic-ui-react/dist/commonjs';
import withDocumentTree from 'components/FolderExplorer/withDocumentTree';

import { findItem } from 'lib/treeUtils';

const enhance = compose(
  withDocumentTree,
  connect(({ folderExplorer: { documentTree } }, { docRefUuid }) => {
    const docRefWithLineage = findItem(documentTree, docRefUuid);
    return {
      docRefWithLineage,
    };
  }, {}),
  branch(({ docRefWithLineage }) => !docRefWithLineage || !docRefWithLineage.node, renderNothing),
);

const DocRefBreadcrumb = ({
  docRefWithLineage: {
    lineage,
    node: { name },
  },
  openDocRef,
}) => (
  <Breadcrumb>
    {lineage.map(l => (
      <React.Fragment key={l.uuid}>
        <Breadcrumb.Divider />
        <Breadcrumb.Section link onClick={() => openDocRef(l)}>
          {l.name}
        </Breadcrumb.Section>
      </React.Fragment>
    ))}

    <Breadcrumb.Divider />
    <Breadcrumb.Section active>{name}</Breadcrumb.Section>
  </Breadcrumb>
);

DocRefBreadcrumb.propTypes = {
  docRefUuid: PropTypes.string.isRequired,
  openDocRef: PropTypes.func.isRequired,
};

export default enhance(DocRefBreadcrumb);