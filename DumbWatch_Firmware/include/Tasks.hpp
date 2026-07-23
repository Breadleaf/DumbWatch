#ifndef TASKS_HPP
#define TASKS_HPP

#include <vector>
#include <functional>

#include "Types.hpp"

namespace tasks
{

    struct BaseTaskData
    {
        const types::TimeMS_t interval = 0;
        types::TimeMS_t lastRun = 0;
    };

    struct ScheduledTaskInterface
    {
        virtual void run(const types::TimeMS_t) = 0;
        virtual ~ScheduledTaskInterface() = default;
    };

    template <class T>
    struct ScheduledTask : public ScheduledTaskInterface
    {
        T data;
        std::function<void(const types::TimeMS_t)> function;

        ScheduledTask(T _data, std::function<void(const types::TimeMS_t)> _function)
            : data(_data), function(_function) {}

        void run(const types::TimeMS_t currentTime) override
        {
            function(currentTime);
        }
    };

} // tasks

#endif // TASKS_HPP