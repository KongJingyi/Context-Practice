import TRTC from "trtc-js-sdk";

/** @typedef {{ sdkAppId: number, userId: string, userSig: string, roomId: string }} TrtcJoinParams */
/** @typedef {{ onLocalStream?: (s: MediaStream) => void, onRemoteStream?: (s: MediaStream) => void, onRemoteUserJoined?: () => void, onError?: (e: Error) => void }} TrtcSessionCallbacks */

let client = null;
/** @type {import('trtc-js-sdk').LocalStream | null} */
let localTrtcStream = null;

export function canUseTrtc(userSig) {
  return Boolean(userSig && !String(userSig).startsWith("DEV_STUB"));
}

/** @param {import('trtc-js-sdk').Stream} stream */
function toMediaStream(stream) {
  if (!stream) return null;
  if (typeof stream.getMediaStream === "function") {
    return stream.getMediaStream();
  }
  if (stream.mediaStream instanceof MediaStream) {
    return stream.mediaStream;
  }
  return null;
}

/** @param {TrtcJoinParams} params @param {TrtcSessionCallbacks} [callbacks] */
export async function startTrtcSession(params, callbacks = {}) {
  await stopTrtcSession();

  client = TRTC.createClient({
    mode: "rtc",
    sdkAppId: params.sdkAppId,
    userId: params.userId,
    userSig: params.userSig,
  });

  client.on("error", (error) => {
    callbacks.onError?.(error instanceof Error ? error : new Error(String(error)));
  });

  client.on("stream-added", async (event) => {
    try {
      await client.subscribe(event.stream);
    } catch (err) {
      callbacks.onError?.(err instanceof Error ? err : new Error(String(err)));
    }
  });

  client.on("stream-subscribed", (event) => {
    const media = toMediaStream(event.stream);
    if (media) {
      callbacks.onRemoteStream?.(media);
      callbacks.onRemoteUserJoined?.();
    }
  });

  await client.join({ strRoomId: params.roomId });

  localTrtcStream = TRTC.createStream({
    userId: params.userId,
    audio: true,
    video: true,
  });
  await localTrtcStream.initialize();
  await client.publish(localTrtcStream);

  const localMedia = toMediaStream(localTrtcStream);
  if (localMedia) {
    callbacks.onLocalStream?.(localMedia);
  }
}

export function setTrtcAudioEnabled(enabled) {
  if (!localTrtcStream) return;
  if (enabled) localTrtcStream.unmuteAudio?.();
  else localTrtcStream.muteAudio?.();
}

export function setTrtcVideoEnabled(enabled) {
  if (!localTrtcStream) return;
  if (enabled) localTrtcStream.unmuteVideo?.();
  else localTrtcStream.muteVideo?.();
}

export async function stopTrtcSession() {
  try {
    if (localTrtcStream && client) {
      await client.unpublish(localTrtcStream).catch(() => {});
    }
    localTrtcStream?.close?.();
    localTrtcStream?.stop?.();
  } catch {
    /* ignore */
  }
  localTrtcStream = null;

  try {
    await client?.leave?.();
  } catch {
    /* ignore */
  }
  client = null;
}
