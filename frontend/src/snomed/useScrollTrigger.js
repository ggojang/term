import React from "react";

function getScrollY(ref) {
  if (ref && ref.pageYOffset !== undefined) {
    return ref.pageYOffset;
  }
  if (ref && ref.scrollTop !== undefined) {
    return ref.scrollTop;
  }
  return document !== undefined
    ? (document.documentElement || document.body.parentNode || document.body)
        .scrollTop
    : 0;
}

function defaultTrigger(next, current, other = {}) {
  const { directional = true, threshold = 100 } = other;
  if (directional) {
    return next < current ? false : !!(next > current && next > threshold);
  }
  return next > threshold;
}

const useScrollTrigger = (options = {}) => {
  const { triggerFunc = defaultTrigger, ...other } = options;
  const [ref, setRef] = React.useState();
  const yRef = React.useRef();
  const [trigger, setTrigger] = React.useState(false);

  const handleScroll = React.useCallback(() => {
    const scrollY = getScrollY(ref);
    setTrigger(triggerFunc(scrollY, yRef.current, other));
    yRef.current = scrollY;
  }, [other, ref, triggerFunc]);

  React.useEffect(() => {
    (ref || window).addEventListener("scroll", handleScroll);
    return () => {
      (ref || window).removeEventListener("scroll", handleScroll);
    };
  }, [handleScroll, ref, setRef]);

  return [trigger, setRef];
};

export default useScrollTrigger;
