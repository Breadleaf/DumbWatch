#ifndef HANDLERINTERFACE_HPP
#define HANDLERINTERFACE_HPP

struct HandlerInterface
{
    virtual void UpdateState() = 0;
    virtual void begin() = 0;
};

#endif // HANDLERINTERFACE_HPP