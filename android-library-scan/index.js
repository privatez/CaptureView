import {PropTypes} from 'react'
import {View, requireNativeComponent} from 'react-native'

var CaptureView = React.createClass({
    propTypes: {
        ...View.props,
    },

    render: function () {
        return (<NativeGradientColorView
            style={this.props.style}
          );
    }
});

var NativeGradientColorView = requireNativeComponent('RCTCaptureView', CaptureView);

module.exports = CaptureView;