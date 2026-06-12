import React from "react";
import PropTypes from 'prop-types';
import { Slide } from "@material-ui/core";
import useScrollTrigger from "./useScrollTrigger";

export default function HideOnScroll(props) {
  const { children, threshold, ...other } = props;
  const [trigger] = useScrollTrigger({ threshold: threshold });
  return (
    <Slide appear={false} direction="down" in={!trigger} {...other}>
      {children}
    </Slide>
  );
}

HideOnScroll.propTypes = {
  children: PropTypes.element.isRequired,
  /**
   * Injected by the documentation to work in an iframe.
   * You won't need it on your project.
   */
  window: PropTypes.func,
};
